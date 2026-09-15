pipeline {
    agent any

    stages {
        stage('Checkout scm') {
            steps {
                checkout scm
            }
        }

        stage('Build marker') {
            steps {
                sh 'git log -5 --oneline && git rev-parse HEAD'
            }
        }

        stage('Slow stage (abort test)') {
            steps {
                sleep 300
            }
        }
    }
}
