Graal native image uses ahead-of-time compilation to build an optimized executable.

### Install GraalVM using sdkman.io

```console
sdk install java 22.3.2.r17-grl
export JAVA_HOME=${SDKMAN_DIR}/candidates/java/current
```

### Install native image support

```console
gu install native-image
```

### Run on the JVM with an agent to capture class metadata

```console
gradle -Pagent run
```

### Copy the metadata as the source configuration

```console
./gradlew metadataCopy --task run --dir src/main/resources/META-INF/native-image
```

### Compile and run the native binary

```console
./gradlew nativeRun
```

### Try the binary yourself at

```console
./build/native/nativeCompile/graal-native
```

### Run the tests against the native binary

```console
./gradlew nativeTest
```
