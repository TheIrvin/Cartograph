# Task 7 implementation report

## Status

Implemented application orchestration and the `POST /api/v1/index` REST contract.

## Changes

- Replaced the placeholder `IndexRepositoryService` port with a Spring service that normalizes URL input, resolves the commit, checks SQLite snapshots first, and fetches/parses/builds/saves only on cache misses.
- Added repository commit resolution to the fetcher port and GitHub adapter so branch cache hits avoid downloading the tree and file contents.
- Added request, graph snapshot, and structured error DTOs.
- Added controller validation and exception mapping for malformed requests, GitHub failures, repository limits, and safe internal errors with correlation IDs logged server-side.
- Registered adapter, parser, graph builder, persistence, and configuration-properties beans; corrected GitHub configuration binding.
- Added offline service and MockMvc controller tests; no real GitHub calls are made.

## Tests

Using Java 17 at `/opt/homebrew/opt/openjdk@17`:

```text
mvn -Dtest='com.tracemap.application.*Test,com.tracemap.api.*Test' test  # PASS (6 tests)
mvn -q test                                                              # PASS
git diff --check                                                         # PASS
```

## Concerns

- Cache misses currently resolve the commit and then invoke the existing fetcher, so the GitHub metadata/commit lookup is repeated on a miss. This keeps the existing fetcher boundary intact and avoids downloading repository contents on cache hits.

## Review follow-up

- Mapped response metrics to the documented `filesSeen`, `filesParsed`, `nodes`, and `edges` JSON names while retaining the domain metric values (`fileCount`, parsed file count, node count, and edge count).
- Scoped HTTP 400 handling to URL/request validation exceptions; unexpected `IllegalArgumentException` failures now use the safe HTTP 500 response.
- Mapped GitHub rate-limit failures to HTTP 429 and added MockMvc regression coverage for both exception boundaries and response metrics.

Focused follow-up tests and the full offline Maven suite pass with Java 17 at `/opt/homebrew/opt/openjdk@17`.
