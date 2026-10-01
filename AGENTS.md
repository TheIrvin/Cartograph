# AGENTS.md

## Repository status

- This is a planning-only workspace for **TraceMap**; there is currently no application code, package manifest, git repository, CI configuration, or runnable test suite.
- Do not invent or run build/test commands until the Wave 0 scaffold adds them. Record any new commands in this file only after they are executable and verified.

## Source of truth

- Start with `README.md`; it links the strategy, feature plan, execution plan, and tracker in intended reading order.
- `tracemap-feature-tracker.csv` is the machine-readable status/dependency/wave source of truth. Feature IDs are permanent (`F{phase}.{nn}`); update status and wave there first, then keep the feature-plan card and changelog consistent.
- Use `tracemap-execution-plan.md` for current sequencing and cut lines. Each wave must end in a runnable demo; do not silently reduce a P0 acceptance criterion.
- The architecture document's original FastAPI/Python table is superseded for v1 by its 2026-10-01 amendment: implementation is TypeScript end-to-end (Next.js route handlers plus a Node indexing worker).

## Planned boundaries

- When scaffolding begins, preserve the planned pnpm monorepo boundaries: `apps/web`, `apps/api`, `packages/parser`, `packages/graph`, `packages/context-assembler`, and `packages/shared-types`.
- The core vertical slice is URL → fetch → parse → resolve/normalize → SQLite snapshot → rendered graph. Test this walking skeleton before UI polish or later infrastructure.
- v1 deliberately excludes Kafka, multi-agent orchestration, and Neo4j; defer them to the gated Phase 2+ work unless the plans are explicitly revised.

## Operational constraints

- Wave 0 expects Node LTS, pnpm, and Python 3.12 locally, pinned in CI once CI exists; the current workspace has none of these project configs yet.
- Planned deployment targets are Vercel for `apps/web` and Fly.io for `apps/api`/indexing workers. Do not provision or deploy until the relevant scaffold and credentials exist.
- `.env.example` is intended to become the canonical local configuration surface; never commit secrets. Planned external secrets belong in Vercel/Fly dashboards.
