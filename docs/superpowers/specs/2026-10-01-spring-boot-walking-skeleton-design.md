# TraceMap Spring Boot Walking Skeleton

**Date:** 2026-10-01  
**Status:** Design approved for specification review  
**Scope:** First implementation slice from the TraceMap execution plan

## Goal

Create the smallest complete TraceMap vertical slice in **Spring Boot with Maven and Java 17**:

```text
GitHub repository URL
  → validate and normalize
  → fetch a bounded repository snapshot
  → parse JavaScript/TypeScript source
  → build a typed call/import graph
  → persist the graph in SQLite
  → return graph JSON over HTTP
```

This slice validates the product's core parsing bet before adding the frontend, asynchronous jobs, chat, authentication, or production deployment.

## Explicit stack decision

The repository's planning documents currently describe a TypeScript/Next.js + Node v1 implementation. This feature intentionally overrides that recommendation for the first implementation: use a Spring Boot backend, Maven, and Java 17. The override should be documented if the project plans are later updated.

## Architecture

Use a single Spring Boot modular monolith with these package boundaries:

```text
...tracemap/
├── api/          REST controllers, DTOs, exception mapping
├── application/  index workflow and ports
├── ingestion/    GitHub URL normalization and fetch adapter
├── parsing/      parser port and JavaScript/TypeScript adapter
├── graph/        symbols, edges, graph building, normalization
├── persistence/ SQLite schema and snapshot adapter
└── common/       narrowly shared primitives and configuration
```

The application layer orchestrates the use case through ports. Controllers call application services; the application service depends on abstractions for fetching, parsing, and persistence; infrastructure adapters implement those abstractions. Domain graph code remains independent of HTTP and GitHub SDK details.

The intended dependency direction is:

```text
api → application → domain/ports
                  ↑       ↑
        ingestion/parsing/persistence adapters
```

The domain and parser code must not depend on controller types or GitHub SDK details. External behavior enters through ports such as:

- `RepositoryFetcher`
- `SourceParser`
- `GraphSnapshotRepository`

The initial implementation is synchronous. Async jobs and a queue are explicitly deferred.

## HTTP contract

### Index repository

```http
POST /api/v1/index
Content-Type: application/json

{
  "repositoryUrl": "https://github.com/owner/repo"
}
```

Successful responses contain:

```json
{
  "repository": "owner/repo",
  "commitSha": "<sha>",
  "nodes": [],
  "edges": [],
  "warnings": [],
  "metrics": {
    "filesSeen": 0,
    "filesParsed": 0,
    "nodes": 0,
    "edges": 0
  }
}
```

The response contract must expose repository identity, reproducible commit identity, graph data, warnings, and basic metrics. DTO names may follow the project's Java naming conventions, but these response concepts are required.

### Error behavior

Return a stable structured error envelope for:

- Malformed or non-GitHub URLs: HTTP 400.
- Repository not found or inaccessible: the appropriate upstream-derived client error, without leaking credentials or stack traces.
- File-count, byte-size, or per-file limit violations: a designed limit error.
- Unsupported source files: skip them and report warnings unless no supported source remains.
- Internal failures: HTTP 500 with a safe public message and server-side diagnostic logging.

Unresolved or dynamic calls are warnings and are never silently represented as proven edges.

## Ingestion rules

- Accept canonical GitHub repository URLs and common `/tree/...` variants, normalizing them to owner, repository, and optional ref.
- Use GitHub REST APIs for repository metadata, recursive tree data, and file contents.
- Support an optional server-side GitHub token through environment configuration; never accept or return a token through the request body or response.
- Enforce repository file-count, total-size, and per-file limits before parsing.
- Capture the latest commit SHA and key snapshots by repository plus commit SHA.
- Keep the first implementation focused on public repositories unless access behavior is explicitly added to the contract.

## Parsing and graph rules

The first adapter supports JavaScript and TypeScript. It extracts:

- Function and method definitions
- Class definitions
- ES module imports and exports
- Direct calls resolvable within the same file

The adapter must preserve source locations and identify unsupported, dynamic, or unresolved constructs as warnings. It must not fabricate cross-file resolution or LLM-inferred structure in this slice.

Stable node IDs are derived from repository identity, commit SHA, file path, symbol kind/name, and source location so repeated indexing of the same snapshot is deterministic.

Edges include an explicit kind and confidence value. Directly proven relationships are distinct from unresolved or inferred relationships; no inferred relationship is required for the first slice.

## SQLite persistence

Start with a replaceable repository port and a minimal SQLite schema:

- `repository_snapshot(repository, commit_sha, indexed_at, metrics_json)`
- `graph_node(snapshot_id, stable_id, kind, name, file_path, start_line, end_line)`
- `graph_edge(snapshot_id, from_id, to_id, kind, confidence)`

The repository must support writing and reading a complete snapshot keyed by repository and commit SHA. A cache hit for an existing snapshot must avoid re-parsing.

## Configuration and safety

- Add an `.env.example` when application configuration is introduced.
- Validate required configuration at startup.
- Keep GitHub credentials server-side and out of logs.
- Do not clone arbitrary user-controlled URLs; only normalized GitHub URLs are accepted.
- Apply bounded fetch and parse limits to prevent unbounded memory, CPU, or disk use.

## Testing strategy

### Unit tests

- GitHub URL normalization and rejection cases
- Repository guardrails and limits
- JavaScript/TypeScript extraction
- Stable node ID generation
- Graph normalization and edge creation
- Structured error mapping

### Integration tests

- Spring application context with SQLite
- Snapshot write/read and cache-hit behavior
- A small checked-in JavaScript/TypeScript fixture repository

### HTTP tests

- MockMvc success response
- Malformed URL response
- Limit violation response
- Safe internal-error response

### Golden fixture

Keep a tiny fixture under test resources with expected nodes and edges. The expected graph is the initial accuracy baseline and should be updated deliberately, not regenerated blindly.

## Acceptance criteria

1. The project builds with Maven on Java 17.
2. `mvn test` passes with unit, integration, and HTTP coverage for the slice.
3. A valid GitHub URL returns a non-empty graph JSON response for a supported fixture/repository.
4. Repeating the same repository and commit lookup reads the SQLite snapshot without re-parsing.
5. Invalid URLs and repository-limit violations return structured, documented errors.
6. Unsupported files and unresolved calls are surfaced as warnings.
7. Structural graph creation does not use an LLM.
8. The parser, fetcher, graph builder, and persistence implementation can be tested independently through their ports.

## Deferred work

Do not include the following in this slice:

- Next.js frontend or React Flow viewer
- Async indexing jobs or queues
- Python parser service
- Authentication and private repositories
- LLM chat, embeddings, or narration
- Redis/KV cache
- Neo4j, Kafka, or cross-service stitching
- Vercel/Fly deployment automation

## Implementation sequencing

1. Scaffold Maven/Spring Boot and Java 17 configuration.
2. Define domain models and ports.
3. Implement URL normalization and fetch limits.
4. Implement the GitHub fetch adapter.
5. Implement the JavaScript/TypeScript parser adapter.
6. Build and normalize graph snapshots.
7. Add SQLite persistence and cache reads.
8. Add REST endpoint and error mapping.
9. Add fixture, unit, integration, and HTTP tests.
10. Run the real Maven checks and document any unavailable external-service checks.
