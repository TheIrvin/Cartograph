# Task 1 Report: Scaffold the Maven Spring Boot application

## Files changed

- `pom.xml`: Added a Java 17 Maven project using Spring Boot 3.5.6, web, validation, SQLite JDBC, Flyway migration, test, and Spring Boot Maven plugin dependencies.
- `.gitignore`: Added common IDE, Maven build, local SQLite data, and local environment-file exclusions while preserving the existing worktree exclusion.
- `.env.example`: Added an empty `GITHUB_TOKEN` configuration placeholder; no secret value is included.
- `src/main/java/com/tracemap/TraceMapApplication.java`: Added the `@SpringBootApplication` entrypoint and `main` method.
- `src/main/resources/application.yml`: Added the SQLite datasource path, port 8080, Flyway location, environment-backed GitHub token, and explicit indexing limits.
- `src/test/java/com/tracemap/TraceMapApplicationTests.java`: Added a `@SpringBootTest` context smoke test with Flyway disabled for the test context and no external GitHub call.

## Tests and commands

Command (with the requested Java 17 environment):

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn test
```

Final result:

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Additional check:

```bash
git diff --check
```

Result: passed with no whitespace errors.

## Concerns

- An initial attempt included `org.flywaydb:flyway-database-sqlite:11.7.2`, but Maven Central did not provide that artifact. It was removed; `flyway-core` remains as the migration dependency. The smoke test explicitly disables Flyway to keep the initial context test independent of migration-provider availability.
- No migration scripts are included in Task 1.

## Review fix report

### Files changed

- `pom.xml`: Added `spring-boot-starter-jdbc` so the configured SQLite datasource is a real runtime datasource.
- `src/main/resources/application.yml`: Made Flyway explicitly disabled by default because `flyway-core` alone does not provide SQLite support; retained the migration dependency for the later SQLite-compatible migration slice. Moved the database path to `tracemap.sqlite.path` so directory setup and datasource configuration share one value.
- `src/main/java/com/tracemap/TraceMapApplication.java`: Added a datasource factory that creates the configured SQLite database parent directory before building the datasource.

### Commands and output

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
PATH="$JAVA_HOME/bin:$PATH" mvn test
```

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Focused clean-startup check:

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
PATH="$JAVA_HOME/bin:$PATH" mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=0
```

```text
Started TraceMapApplication ...
Tomcat started on port 52806 (http)
```

The process was stopped by the check timeout after successful startup. `git diff --check` also passed.

### Concerns

- Flyway is intentionally disabled until Task 6 adds and verifies a SQLite-compatible migration approach; no runtime claim is made that Flyway migrations currently run against SQLite.
- No schema or persistence implementation was added.
