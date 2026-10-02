# AGENTS.md

## Repository status

- This repository contains the verified Wave 1 TraceMap walking skeleton.
- The current implementation override is Java 17 + Maven + Spring Boot 3.5.6 (the earlier TypeScript/Next.js architecture remains planning context, not the active scaffold).
- The application entry point is `com.tracemap.TraceMapApplication`; the offline test suite runs with `mvn test`.

## Verified local commands

Use Homebrew's Java 17 before running Maven:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn spring-boot:run
mvn test
```

The HTTP endpoint is `POST /api/v1/index` with a JSON `repositoryUrl`. SQLite persists snapshots at `./data/tracemap.db` by default; set `tracemap.sqlite.path` to override it.

## Source of truth

- Start with `README.md`; it links the strategy, feature plan, execution plan, and tracker in intended reading order.
- `tracemap-feature-tracker.csv` is the machine-readable status/dependency/wave source of truth. Feature IDs are permanent (`F{phase}.{nn}`); update status and wave there first, then keep the feature-plan card and changelog consistent.
- Use `tracemap-execution-plan.md` for current sequencing and cut lines. Each wave must end in a runnable demo; do not silently reduce a P0 acceptance criterion.
- The architecture document's original FastAPI/Python table and its TypeScript amendment are planning references; the active walking-skeleton implementation uses the Spring Boot override above.

## Planned boundaries

- Preserve the planned product boundaries when later work expands beyond this scaffold; the current vertical slice is intentionally contained in the Spring Boot application.
- The core vertical slice is URL → fetch → parse → resolve/normalize → SQLite snapshot → rendered graph. Test this walking skeleton before UI polish or later infrastructure.
- v1 deliberately excludes Kafka, multi-agent orchestration, and Neo4j; defer them to the gated Phase 2+ work unless the plans are explicitly revised.

## Operational constraints

- The current scaffold requires Java 17 and Maven. Node, pnpm, and Python remain future planning assumptions and are not required by the current tests.
- Planned deployment targets are Vercel for `apps/web` and Fly.io for `apps/api`/indexing workers. Do not provision or deploy until the relevant scaffold and credentials exist.
- `.env.example` is intended to become the canonical local configuration surface; never commit secrets. Planned external secrets belong in Vercel/Fly dashboards.
