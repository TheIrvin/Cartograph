# TraceMap — Spring Boot walking skeleton

> Real call graphs for any repository — accurate, not hallucinated. Paste a GitHub URL, get an interactive, AST-derived call graph with click-to-trace paths.

## Start here

| If you want to know… | Read |
|---|---|
| What is this product and why does it win? | [`tracemap-architecture-and-implementation-plan.md`](./tracemap-architecture-and-implementation-plan.md) — strategy: positioning, architecture, differentiation (§7 carries the 2026-10-01 stack amendment) |
| What exactly gets built, feature by feature? | [`tracemap-detailed-implementation-plan.md`](./tracemap-detailed-implementation-plan.md) — 90 features with IDs, acceptance criteria, cross-cutting specs (v1.1) |
| What do I build this week, and in what order? | [`tracemap-execution-plan.md`](./tracemap-execution-plan.md) — situation analysis, 12-wave schedule, deep specs (v2.1) |
| What's the status of any feature? | [`tracemap-feature-tracker.csv`](./tracemap-feature-tracker.csv) — one row per feature |

## Status snapshot (2026-10-02)

- **Stage:** Backend walking skeleton merged into `main`; Java 17 test suite passes 60 tests. This does not complete the original Wave 0/1 gates: no viewer, CI, deployment, or live GitHub end-to-end verification yet.
- **Live smoke check (2026-10-02):** `sindresorhus/is` indexed successfully through `POST /api/v1/index` with 19 files seen, 5 parsed, 242 nodes, and 487 edges; a repeated request returned the same SQLite-cached snapshot.
- **Tracker:** 17 features are In Progress and 73 Not Started. Partial backend implementations do not satisfy the full feature cards; planned completion waves are unchanged.
- **Current endpoint:** `POST /api/v1/index` accepts `{ "repositoryUrl": "https://github.com/<owner>/<repo>" }` and returns the graph snapshot, commit SHA, warnings, and metrics.
- **Database:** SQLite defaults to `./data/tracemap.db`; override with `tracemap.sqlite.path`.

## Run and test locally

From the repository root on macOS with Homebrew Java 17:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn spring-boot:run
```

In another terminal, call the indexing endpoint:

```bash
curl -X POST http://localhost:8080/api/v1/index \
  -H 'Content-Type: application/json' \
  -d '{"repositoryUrl":"https://github.com/<owner>/<repo>"}'
```

Run the complete offline test suite with the same Java 17 setup:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn test
```

### API errors

Errors use a stable `{ "code": "...", "message": "..." }` body. Current mappings are:

| HTTP status | code | Meaning |
|---|---|---|
| 400 | `INVALID_REQUEST` | Invalid JSON, blank URL, or invalid GitHub URL |
| 413 | `REPOSITORY_LIMIT_EXCEEDED` | Repository exceeds configured file/count/byte limits |
| 404 | `UPSTREAM_GITHUB_ERROR` | GitHub repository or resource was not found |
| 403 | `UPSTREAM_GITHUB_ERROR` | GitHub denied access |
| 429 | `UPSTREAM_GITHUB_ERROR` | GitHub rate limit was exceeded |
| 502 | `UPSTREAM_GITHUB_ERROR` | GitHub returned an unusable or unexpected response |
| 500 | `INTERNAL_ERROR` | Unexpected server failure |

Example limit response:

```json
{"code":"REPOSITORY_LIMIT_EXCEEDED","message":"Repository exceeds indexing limits."}
```

## Tracker columns

`id` (permanent `F{phase}.{nn}`) · `feature` · `epic` · `phase` · `priority` (P0–P3) · `effort` (XS–XL) · `depends_on` · `status` (⬜🟡🔵✅🚫❌) · `wave` (the wave where the feature's **full acceptance criteria** pass; minimal-first slices land earlier and stay 🟡 — see feature plan §0.4).

Waves W0–W11 + `P1-tail` cover everything through launch; `Gate C+` (Phase 2) and `Gate D+` (Phase 3) features are blocked behind their metric gates.
