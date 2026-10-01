# Task 4 report

Status: complete

Implemented the GitHub adapter boundary with typed metadata, commit, recursive tree, and content calls. The fetcher resolves the requested/default branch, filters JavaScript/TypeScript candidates, validates tree limits before content requests, and maps responses into the shared graph model. Token headers are optional and ETag/304, not-found, and rate-limit responses are represented by adapter errors.

Tests are fully offline using `com.sun.net.httpserver.HttpServer`; they verify request headers, typed responses, error mapping, content decoding, and that limit rejection occurs before content download.

Focused command (Java 17 Homebrew):

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.20.1/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn -Dtest='com.tracemap.ingestion.github.*Test' test
```

Result: 5 tests passed.

Concern: Spring configuration registration/wiring of `GitHubProperties`, `GitHubClient`, and the fetcher is intentionally left for orchestration/configuration work; this task only provides the adapter types and constructors.
