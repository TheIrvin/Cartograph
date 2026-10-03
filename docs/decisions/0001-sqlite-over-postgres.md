# ADR 0001: SQLite over Postgres for the v1 graph store

**Status:** Accepted (2026-10-03)
**Deciders:** pacman-cli
**Scope:** persistence adapter only — the `GraphSnapshotRepository` port is unchanged

## Context

Cartograph's store holds immutable graph snapshots keyed by `(repository, commitSha)`.
Access is write-once per commit, read-by-key — a snapshot cache, not a relational workload.
The v1 plan deliberately excludes server infrastructure, and `F2.07 Neo4j migration`
(Gate C+) is the planned graph-store evolution; Postgres was never on that path.

## Decision

Keep SQLite as the only persistence adapter through v1. Do not introduce Postgres
without a trigger from the list below.

## Rationale

- **Workload fit.** Snapshot upsert + lookup by `(repository, commitSha)` is exactly
  what SQLite is best at. There are no concurrent writers, joins, or analytical queries.
- **Product story.** The zero-install quickstart ("no database to install"), the offline
  test suite, and the one-command demo all depend on embedded storage.
- **Planned evolution.** The architecture already routes store evolution to a graph
  database at Gate C+. An unplanned SQLite → Postgres → Neo4j sequence is two migrations
  bought for no current benefit.
- **Escape hatch is real.** The store sits behind a port. A Postgres adapter is one new
  adapter plus Flyway migrations (native in flyway-core) with zero changes to the
  application core — the swap stays bounded whenever it is justified.

## Triggers to revisit (any one justifies the swap)

1. **Multiple instances or async jobs (F0.12, deploy pipeline).** Two API instances
   cannot share a SQLite file; shared state requires a server DB or a queue.
2. **Server-side graph queries (Wave 9+ trace engine, F0.43 embeddings).** Recursive
   path queries over large graphs, and pgvector for semantic search, earn Postgres its keep.
3. **Ops threshold.** Managed HA/backup requirements, or outgrowing a single Fly.io
   volume.

## When exactly do we shift?

Mapped to the plan's waves so the moment is unambiguous:

| The moment | Expected wave | Why it forces the move |
|---|---|---|
| Async indexing jobs (F0.12) run in a separate worker process, **or** the deploy pipeline (F0.04) scales the API past one Fly.io instance | W4–W8 | Two processes cannot share one SQLite file — shared state requires a server DB. **Most likely shift point.** |
| Ask/chat needs embeddings (F0.43) | W7 | Vector search is pgvector territory; SQLite has no good answer. |
| Trace engine needs server-side recursive path queries (F1.01–F1.05) | W9–W10 | Doable in SQLite CTEs at small scale; this is where Postgres (or the planned Neo4j move at Gate C) earns its keep. |
| Self-hosters/customers demand managed HA + backups | Gate C+ anytime | Operational threshold, not a technical one. |

Every wave before those moments — including async jobs at W4 with an in-process
queue and a single instance — is buildable on SQLite. If F0.12 lands with jobs
in-process and deployment stays single-instance, the shift moves later than W4;
the deciding fact is always *concurrent processes sharing state*, not the calendar.

## Consequences

- Fly.io deployment needs a persistent volume for the SQLite file while this decision holds.
- Flyway stays explicitly disabled (flyway-core lacks SQLite support); schema remains
  explicit SQL setup until a swap or a SQLite-compatible migration path lands.
- This ADR is the reference answer for "why not Postgres?" — revisit it when a trigger
  fires, not on vibes.
