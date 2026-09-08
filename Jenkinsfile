pipeline {
    agent any

    parameters {
        string(name: 'BUILD_REF', defaultValue: 'build-3', description: 'Git ref to checkout for this build')
        string(name: 'BASELINE_REF', defaultValue: 'build-2', description: 'Previous build ref; leave blank for first build')
    }

    environment {
        REPO_URL = 'https://github.com/harness-testing/sei-jenkins-scm-correlation-demo.git'
        REPO_RELEASES_URL = 'https://github.com/harness-testing/sei-jenkins-scm-correlation-demo/releases'
    }

    stages {
        stage('Checkout') {
            steps {
                sh '''
                    rm -rf scm-demo-src
                    git clone "${REPO_URL}" scm-demo-src
                    cd scm-demo-src
                    git fetch --tags --force
                    git checkout --detach "${BUILD_REF}"
                '''
            }
        }

        stage('Resolve SCM Commit IDs') {
            steps {
                dir('scm-demo-src') {
                    script {
                        String targetSha = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                        String baselineRef = params.BASELINE_REF?.trim() ?: ''
                        List<String> commitIds = []

                        if (baselineRef) {
                            String baselineSha = sh(script: "git rev-parse ${baselineRef}", returnStdout: true).trim()
                            int ancestorStatus = sh(
                                script: "git merge-base --is-ancestor ${baselineSha} ${targetSha}",
                                returnStatus: true
                            )
                            if (ancestorStatus != 0) {
                                error("Baseline ${baselineRef} (${baselineSha}) is not an ancestor of target (${targetSha})")
                            }

                            String revList = sh(
                                script: "git rev-list --reverse ${baselineSha}..${targetSha}",
                                returnStdout: true
                            ).trim()
                            if (revList) {
                                commitIds = revList.split('\n').findAll { it?.trim() }
                            } else {
                                commitIds = [targetSha]
                            }
                            env.BASELINE_SHA = baselineSha
                        } else {
                            String revList = sh(
                                script: "git rev-list --reverse ${targetSha}",
                                returnStdout: true
                            ).trim()
                            if (revList) {
                                commitIds = revList.split('\n').findAll { it?.trim() }
                            } else {
                                commitIds = [targetSha]
                            }
                            env.BASELINE_SHA = ''
                        }

                        commitIds.each { String commitId ->
                            if (!(commitId ==~ /[0-9a-f]{40}/)) {
                                error("Invalid commit ID (expected 40-char hex): ${commitId}")
                            }
                        }

                        env.TARGET_SHA = targetSha
                        env.SEI_SCM_COMMIT_IDS = commitIds.join(',')

                        echo "Baseline ref: ${baselineRef ?: '(none)'}"
                        echo "Baseline SHA: ${env.BASELINE_SHA ?: '(none)'}"
                        echo "Target ref: ${params.BUILD_REF}"
                        echo "Target SHA: ${targetSha}"
                        echo "SEI_SCM_COMMIT_IDS: ${env.SEI_SCM_COMMIT_IDS}"
                    }
                }
            }
        }

        stage('Compile and Smoke Test') {
            steps {
                dir('scm-demo-src') {
                    sh '''
                        mkdir -p target/classes
                        find src/main/java -name '*.java' -print | xargs javac --release 17 -d target/classes
                        printf 'Main-Class: com.harness.demo.DemoApplication\n' > target/MANIFEST.MF
                        jar cfm target/sei-scm-demo.jar target/MANIFEST.MF -C target/classes .
                        java -jar target/sei-scm-demo.jar
                    '''
                }
            }
        }

        stage('Create Artifact Manifest') {
            steps {
                dir('scm-demo-src') {
                    script {
                        String digest = sh(
                            script: "shasum -a 256 target/sei-scm-demo.jar | awk '{print $1}'",
                            returnStdout: true
                        ).trim()
                        String targetShortSha = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        String qualifier = "${params.BUILD_REF}-${targetShortSha}"
                        String expectedScmCommitIds = env.SEI_SCM_COMMIT_IDS

                        writeFile file: 'artifact-manifest.json', text: """{
  "artifacts": [
    {
      "type": "jar",
      "location": "${env.REPO_RELEASES_URL}",
      "name": "sei-scm-demo",
      "qualifier": "${qualifier}",
      "hash": "sha256:${digest}",
      "input": false,
      "output": true,
      "metadata": {
        "build_ref": "${params.BUILD_REF}",
        "baseline_ref": "${params.BASELINE_REF ?: ''}",
        "target_sha": "${env.TARGET_SHA}",
        "expected_scm_commit_ids": "${expectedScmCommitIds}"
      }
    }
  ]
}"""
                        echo "artifact-manifest.json created with sha256:${digest}"
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                dir('scm-demo-src') {
                    archiveArtifacts artifacts: 'target/sei-scm-demo.jar,artifact-manifest.json', fingerprint: true
                }
            }
        }

        stage('Report SCM Correlation') {
            steps {
                script {
                    List<String> ids = env.SEI_SCM_COMMIT_IDS.split(',') as List<String>
                    echo "scmCommitIds after explicit merge = ${ids}"
                }
            }
        }
    }
}
