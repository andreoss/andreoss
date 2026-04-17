# ZIO HTTP Native

A zio-http + zio-quill example compiled to a GraalVM native image.
The single endpoint runs `SELECT current_timestamp` through Quill and returns it as JSON.

## Run

```bash
./app-native
curl http://localhost:8080
{"status":"OK","version":"1.0.0","timestamp":"2026-09-09 22:16:40.062487"}
```

Configured at runtime with `POSTGRES_HOST`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`.

## Build

```bash
sbt compile
GRAALVM_HOME=/path/to/graalvm sbt nativeImage
```

## Versions

- zio-http: 3.10.1
- zio: 2.1.24
- quill: 4.8.6
- Scala: 3.8.1
- sbt: 1.12.15
- GraalVM: 25.0.2
