# Task 2 implementation report

## Scope

Implemented the TraceMap domain model and application-port boundary requested by Task 2. The implementation is limited to Java records/enums, application interfaces, and focused tests. No URL parsing, GitHub adapter, parser adapter, persistence, or controller code was added.

## Domain model

Added the following immutable domain values under `com.tracemap.graph.model`:

- `SymbolKind` and `EdgeKind` enums.
- `GraphNode` with stable ID, symbol kind, name, source path, and ordered source lines.
- `GraphEdge` with source/target IDs, edge kind, and a bounded confidence score.
- `GraphSnapshot` keyed by repository and commit SHA, containing nodes, edges, warnings, and metrics.
- `GraphMetrics` and `GraphWarning` records.
- `RepositoryRef`, `RepositorySnapshot`, `SourceFile`, and `ParsedFile` records used by ports.

Collection-valued records defensively copy inputs with `List.copyOf`, preserving immutable value semantics. Basic invariants reject invalid source locations, confidence values, warning lines, and negative metrics.

## Application ports

Added Spring/GitHub-independent interfaces under `com.tracemap.application`:

- `RepositoryFetcher`: fetches a `RepositorySnapshot` from a `RepositoryRef`.
- `SourceParser`: parses a `SourceFile` into a `ParsedFile`.
- `GraphSnapshotRepository`: finds and saves `GraphSnapshot` values.
- `IndexRepositoryService`: application indexing entry point accepting a `RepositoryRef`.

## Tests

Added focused tests for:

- Stable graph node IDs and source locations.
- Edge kind and confidence preservation.
- Snapshot repository/commit identity, warnings, and metrics.
- Immutable collection fields.
- Port signatures and domain-record flow.

Commands run with Java 17:

```text
mvn -q -Dtest='com.tracemap.graph.**' test  # PASS
mvn -q test                                  # PASS
```

## Self-review

- No Spring Web, Spring annotations, GitHub SDK types, or adapter response types appear in application ports.
- Existing Task 1 scaffold files were preserved.
- `git diff --check` passed.

## Concerns

The brief did not prescribe packages or field lists for the supporting records (`RepositoryRef`, `RepositorySnapshot`, `SourceFile`, and `ParsedFile`), so they are colocated in `com.tracemap.graph.model` as neutral domain values. Task 3 may need to adapt its planned `com.tracemap.ingestion.RepositoryRef` to this port-facing type or introduce an explicit mapping.
