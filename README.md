# SEI Jenkins SCM Correlation Demo

A minimal, dependency-free Java 17 project that demonstrates how a Jenkins pipeline computes
`SEI_SCM_COMMIT_IDS` — the Git commits introduced since the previous successful build.

The pipeline uses only `git`, `javac`, `jar`, and `java`. No Maven, Gradle, or Spring.

## Repository layout

```
src/main/java/com/harness/demo/   # Application source (grows across commits A–E)
Jenkinsfile                       # Declarative pipeline with SCM correlation logic
```

## Jenkins parameters

| Parameter       | Default   | Purpose                                              |
|-----------------|-----------|------------------------------------------------------|
| `BUILD_REF`     | `build-3` | Git ref to checkout (tag or SHA) for this build      |
| `BASELINE_REF`  | `build-2` | Previous build ref; blank for the first build        |

## Three demo builds

Run the pipeline three times in order. Each run checks out `BUILD_REF` and computes commits
**since** `BASELINE_REF` (exclusive..inclusive range). These are commits *introduced since the
previous successful build*, not every historical commit whose code remains in the JAR.

### Build 1 — first build (commit A only)

```
BUILD_REF=build-1
BASELINE_REF=          (blank)
```

- Resolves all commits reachable from `build-1` → **commit A**
- `SEI_SCM_COMMIT_IDS` = SHA of commit A

### Build 2 — after greeting features (commits B, C)

```
BUILD_REF=build-2
BASELINE_REF=build-1
```

- Range `build-1..build-2` → **commits B and C**
- Commit B adds `GreetingService`; commit C adds input normalization

### Build 3 — after build metadata (commits D, E)

```
BUILD_REF=build-3
BASELINE_REF=build-2
```

- Range `build-2..build-3` → **commits D and E**
- Commit D adds `BuildInfo`; commit E adds the build summary line

## Local build (no Jenkins)

```bash
mkdir -p target/classes
find src/main/java -name '*.java' | xargs javac --release 17 -d target/classes
echo 'Main-Class: com.harness.demo.DemoApplication' > target/MANIFEST.MF
jar cfm target/sei-scm-demo.jar target/MANIFEST.MF -C target/classes .
java -jar target/sei-scm-demo.jar
```

## Tags

| Tag      | Points to | Contains through commit |
|----------|-----------|-------------------------|
| `build-1`| A         | Initial app             |
| `build-2`| C         | + GreetingService, normalization |
| `build-3`| E         | + BuildInfo, build summary       |
