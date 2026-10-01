# Spring Boot Walking Skeleton Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a synchronous Spring Boot + Maven + Java 17 vertical slice that validates a GitHub URL, fetches a bounded JavaScript/TypeScript repository snapshot, builds a deterministic graph, persists it in SQLite, and returns graph JSON.

**Architecture:** A modular monolith separates API, application orchestration, domain graph logic, and infrastructure adapters. The application service depends on `RepositoryFetcher`, `SourceParser`, and `GraphSnapshotRepository` ports; GitHub, parser, and SQLite implementations sit behind those ports. The first implementation is synchronous and public-repository focused.

**Tech Stack:** Java 17, Spring Boot, Maven, Spring Web, Bean Validation, Spring Test/MockMvc, SQLite JDBC, Flyway or equivalent schema migration, and a tree-sitter-compatible JavaScript/TypeScript parser adapter.

---

## Task 1: Scaffold the Maven Spring Boot application

**Files:**
- Create: `pom.xml`
- Create: `.gitignore`
- Create: `.env.example`
- Create: `src/main/java/com/tracemap/TraceMapApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/test/java/com/tracemap/TraceMapApplicationTests.java`

- [ ] **Step 1: Define the Maven project and dependencies**

Create a Java 17 Maven project with Spring Boot dependency management and dependencies for web, validation, testing, SQLite JDBC, and database migration. Pin the Spring Boot version to a current compatible 3.x release and configure the Maven compiler for Java 17.

- [ ] **Step 2: Add the Spring Boot entrypoint**

Create `com.tracemap.TraceMapApplication` with `@SpringBootApplication` and a standard `main` method.

- [ ] **Step 3: Add safe local configuration defaults**

Configure SQLite at `./data/tracemap.db`, server port `8080`, and explicit indexing limits for maximum files, total bytes, and per-file bytes. Read `GITHUB_TOKEN` only from environment/configuration; never place a secret in `.env.example`.

- [ ] **Step 4: Add the context smoke test**

Use `@SpringBootTest` to verify that the application context starts with test configuration and no external GitHub call.

- [ ] **Step 5: Run the first check**

Run:

```bash
mvn test
```

Expected: BUILD SUCCESS with the context test passing.

---

## Task 2: Define domain models and application ports

**Files:**
- Create: `src/main/java/com/tracemap/graph/model/SymbolKind.java`
- Create: `src/main/java/com/tracemap/graph/model/EdgeKind.java`
- Create: `src/main/java/com/tracemap/graph/model/GraphNode.java`
- Create: `src/main/java/com/tracemap/graph/model/GraphEdge.java`
- Create: `src/main/java/com/tracemap/graph/model/GraphSnapshot.java`
- Create: `src/main/java/com/tracemap/graph/model/GraphMetrics.java`
- Create: `src/main/java/com/tracemap/graph/model/GraphWarning.java`
- Create: `src/main/java/com/tracemap/application/RepositoryFetcher.java`
- Create: `src/main/java/com/tracemap/application/SourceParser.java`
- Create: `src/main/java/com/tracemap/application/GraphSnapshotRepository.java`
- Create: `src/main/java/com/tracemap/application/IndexRepositoryService.java`

- [ ] **Step 1: Write model tests first**

Test that graph records preserve stable IDs, source locations, edge kind, confidence, warnings, and metrics. Test that a snapshot is keyed by `repository` and `commitSha`.

- [ ] **Step 2: Define immutable records and enums**

Use Java records for request-independent domain values. `GraphNode` must contain `stableId`, `kind`, `name`, `filePath`, `startLine`, and `endLine`. `GraphEdge` must contain `fromId`, `toId`, `kind`, and `confidence`. `GraphSnapshot` must contain repository, commit SHA, nodes, edges, warnings, and metrics.

- [ ] **Step 3: Define ports**

Define ports with no Spring Web or GitHub SDK types:

```java
RepositorySnapshot fetch(RepositoryRef ref);
ParsedFile parse(SourceFile file);
Optional<GraphSnapshot> find(String repository, String commitSha);
void save(GraphSnapshot snapshot);
```

Use domain records such as `RepositoryRef`, `RepositorySnapshot`, `SourceFile`, and `ParsedFile` rather than leaking adapter-specific response objects.

- [ ] **Step 4: Run focused tests**

Run:

```bash
mvn -Dtest='com.tracemap.graph.**' test
```

Expected: PASS.

---

## Task 3: Implement GitHub URL normalization and guardrails

**Files:**
- Create: `src/main/java/com/tracemap/ingestion/GitHubUrlNormalizer.java`
- Create: `src/main/java/com/tracemap/ingestion/RepositoryRef.java`
- Create: `src/main/java/com/tracemap/ingestion/IndexingLimits.java`
- Create: `src/main/java/com/tracemap/ingestion/RepositoryLimitException.java`
- Test: `src/test/java/com/tracemap/ingestion/GitHubUrlNormalizerTest.java`
- Test: `src/test/java/com/tracemap/ingestion/IndexingLimitsTest.java`

- [ ] **Step 1: Add URL fixture cases**

Cover canonical URLs, trailing slashes, `/tree/{branch}` URLs, query/fragment removal, malformed paths, non-GitHub hosts, missing owner/repository, and encoded path traversal attempts.

- [ ] **Step 2: Implement normalization**

Accept only HTTPS GitHub repository URLs. Normalize owner/repository, preserve an optional branch/ref, reject extra repository path segments except supported `/tree/...` forms, and return a typed `RepositoryRef`.

- [ ] **Step 3: Implement configurable limits**

Provide maximum file count, total bytes, and per-file bytes. Expose a method that validates a fetched tree before content download and throws a typed limit exception with the violated limit and observed value.

- [ ] **Step 4: Run focused tests**

Run:

```bash
mvn -Dtest='com.tracemap.ingestion.*Test' test
```

Expected: PASS.

---

## Task 4: Implement the GitHub REST fetch adapter

**Files:**
- Create: `src/main/java/com/tracemap/ingestion/github/GitHubRepositoryFetcher.java`
- Create: `src/main/java/com/tracemap/ingestion/github/GitHubClient.java`
- Create: `src/main/java/com/tracemap/ingestion/github/GitHubProperties.java`
- Create: `src/main/java/com/tracemap/ingestion/github/GitHubFetchException.java`
- Test: `src/test/java/com/tracemap/ingestion/github/GitHubRepositoryFetcherTest.java`
- Test: `src/test/java/com/tracemap/ingestion/github/GitHubClientTest.java`

- [ ] **Step 1: Define the GitHub client boundary**

Use Spring’s HTTP client with typed methods for repository metadata, recursive tree retrieval, and file-content retrieval. Keep GitHub response DTOs inside the `github` adapter package.

- [ ] **Step 2: Implement metadata and commit resolution**

Resolve the requested ref or default branch to a commit SHA. Return a `RepositorySnapshot` containing repository identity, commit SHA, and a bounded list of source files.

- [ ] **Step 3: Enforce limits before downloading content**

Inspect tree entries first, count only supported candidate files for parsing, reject the repository when configured file/byte limits are exceeded, then download eligible file contents.

- [ ] **Step 4: Add ETag support without changing the port**

Pass `If-None-Match` when an ETag is available and handle `304` as a cacheable upstream response. Do not expose HTTP-client details to application code.

- [ ] **Step 5: Mock upstream behavior in tests**

Use a local mock HTTP server or Spring HTTP client mock to verify request paths, token header behavior, 404 mapping, rate-limit failure mapping, ETag headers, and limit enforcement. Tests must not call GitHub.

- [ ] **Step 6: Run focused tests**

Run:

```bash
mvn -Dtest='com.tracemap.ingestion.github.*Test' test
```

Expected: PASS.

---

## Task 5: Implement JavaScript/TypeScript parsing and graph construction

**Files:**
- Create: `src/main/java/com/tracemap/parsing/javascript/JavaScriptTypeScriptParser.java`
- Create: `src/main/java/com/tracemap/parsing/javascript/JavaScriptParserProperties.java`
- Create: `src/main/java/com/tracemap/graph/GraphBuilder.java`
- Create: `src/main/java/com/tracemap/graph/StableNodeId.java`
- Test: `src/test/java/com/tracemap/parsing/javascript/JavaScriptTypeScriptParserTest.java`
- Test: `src/test/java/com/tracemap/graph/GraphBuilderTest.java`
- Add fixture: `src/test/resources/fixtures/simple-ts-repo/`

- [ ] **Step 1: Add the golden fixture**

Create a tiny TypeScript fixture with two functions, one class method, an import/export, a direct same-file call, and one dynamic/unresolved call. Store expected node and edge data beside or alongside the test.

- [ ] **Step 2: Integrate the parser library behind `SourceParser`**

Parse only `.js`, `.jsx`, `.ts`, and `.tsx` files. Return source locations, definitions, imports/exports, direct calls, and warnings. Unsupported syntax must produce a warning rather than an invented relationship.

- [ ] **Step 3: Implement deterministic node IDs**

Hash or compose repository, commit SHA, normalized path, symbol kind/name, and start location. Identical input must produce identical IDs across runs.

- [ ] **Step 4: Build the graph**

Create nodes for extracted definitions and proven same-file call/import edges. Use confidence `1.0` for directly proven edges. Preserve unresolved calls in warnings and do not emit them as proven edges.

- [ ] **Step 5: Assert golden output**

Test exact expected node names/locations, edge kinds, deterministic IDs, warning presence, and metrics. Avoid snapshot regeneration as the test oracle.

- [ ] **Step 6: Run focused tests**

Run:

```bash
mvn -Dtest='com.tracemap.parsing.javascript.*Test,com.tracemap.graph.*Test' test
```

Expected: PASS.

---

## Task 6: Add SQLite persistence and snapshot cache

**Files:**
- Create: `src/main/resources/db/migration/V1__create_graph_snapshot_tables.sql`
- Create: `src/main/java/com/tracemap/persistence/sqlite/SQLiteGraphSnapshotRepository.java`
- Create: `src/main/java/com/tracemap/persistence/sqlite/GraphSnapshotRowMapper.java`
- Test: `src/test/java/com/tracemap/persistence/sqlite/SQLiteGraphSnapshotRepositoryTest.java`

- [ ] **Step 1: Create the schema migration**

Create `repository_snapshot`, `graph_node`, and `graph_edge` tables with a unique repository/commit key, foreign keys, indexes for snapshot lookup and edge traversal, and explicit confidence storage.

- [ ] **Step 2: Implement writes transactionally**

Insert the snapshot, nodes, and edges in one transaction. Reindexing the same repository/commit must be idempotent and must not create duplicate graph rows.

- [ ] **Step 3: Implement complete reads**

Load one complete snapshot by repository and commit SHA, reconstructing metrics and warnings. Keep serialization details inside persistence.

- [ ] **Step 4: Test cache behavior**

Verify write/read round trips, empty lookup, duplicate write behavior, foreign-key cleanup, and preservation of warning/metric data.

- [ ] **Step 5: Run focused tests**

Run:

```bash
mvn -Dtest='com.tracemap.persistence.sqlite.*Test' test
```

Expected: PASS.

---

## Task 7: Implement application orchestration and REST API

**Files:**
- Create: `src/main/java/com/tracemap/application/IndexRepositoryService.java`
- Create: `src/main/java/com/tracemap/api/IndexRepositoryRequest.java`
- Create: `src/main/java/com/tracemap/api/GraphSnapshotResponse.java`
- Create: `src/main/java/com/tracemap/api/ApiErrorResponse.java`
- Create: `src/main/java/com/tracemap/api/IndexController.java`
- Create: `src/main/java/com/tracemap/api/ApiExceptionHandler.java`
- Test: `src/test/java/com/tracemap/application/IndexRepositoryServiceTest.java`
- Test: `src/test/java/com/tracemap/api/IndexControllerTest.java`

- [ ] **Step 1: Test cache-first orchestration**

Mock the ports and verify that a cache hit returns without fetch or parse calls. For a miss, verify fetch → parse → graph build → save → response ordering.

- [ ] **Step 2: Implement the application service**

Normalize the URL, resolve the commit, check the snapshot repository, fetch only on a miss, parse supported files, build the graph, persist it, and return the snapshot. Aggregate warnings without converting them into proven edges.

- [ ] **Step 3: Add the controller contract**

Implement `POST /api/v1/index` with Bean Validation for a nonblank URL and map the application result to the documented response shape.

- [ ] **Step 4: Add structured exception mapping**

Map invalid URLs to 400, repository-not-found/upstream access failures to the appropriate client response, limit failures to a documented 413-style error, and unexpected failures to a safe 500 response with a correlation ID in logs only.

- [ ] **Step 5: Run controller tests**

Run:

```bash
mvn -Dtest='com.tracemap.application.*Test,com.tracemap.api.*Test' test
```

Expected: PASS.

---

## Task 8: Add the end-to-end fixture test and verification documentation

**Files:**
- Create: `src/test/java/com/tracemap/IndexRepositoryEndToEndTest.java`
- Modify: `README.md`
- Modify: `AGENTS.md`

- [ ] **Step 1: Wire test adapters**

Use a fake GitHub fetcher backed by the checked-in fixture and the real parser, graph builder, Spring context, and SQLite repository. Do not require network access for the test.

- [ ] **Step 2: Verify the complete flow**

Assert that the HTTP request returns non-empty nodes and edges, the commit SHA is present, warnings are structured, and a second identical request hits persistence without invoking the fake fetcher again.

- [ ] **Step 3: Run the full check**

Run:

```bash
mvn test
```

Expected: BUILD SUCCESS with all unit, integration, and HTTP tests passing.

- [ ] **Step 4: Document only verified commands**

Update `README.md` with the exact Java 17/Maven startup and test commands. Update `AGENTS.md` only with commands and configuration facts that actually work in the new scaffold. Do not document deployment commands yet.

## Final self-review checklist

- [ ] Every requirement in the design spec is covered by a task.
- [ ] No task introduces the deferred frontend, async queue, LLM, authentication, Neo4j, Kafka, or deployment work.
- [ ] All tests are offline and deterministic except explicitly manual smoke checks.
- [ ] Maven and Java 17 commands are run from the repository root.
- [ ] If git is initialized later, make one conventional commit after each coherent task; the current workspace has no git repository, so commits cannot be created yet.
