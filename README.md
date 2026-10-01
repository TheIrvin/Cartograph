# TraceMap — Planning Workspace

> Real call graphs for any repository — accurate, not hallucinated. Paste a GitHub URL, get an interactive, AST-derived call graph with click-to-trace paths.

## Start here

| If you want to know… | Read |
|---|---|
| What is this product and why does it win? | [`tracemap-architecture-and-implementation-plan.md`](./tracemap-architecture-and-implementation-plan.md) — strategy: positioning, architecture, differentiation (§7 carries the 2026-10-01 stack amendment) |
| What exactly gets built, feature by feature? | [`tracemap-detailed-implementation-plan.md`](./tracemap-detailed-implementation-plan.md) — 90 features with IDs, acceptance criteria, cross-cutting specs (v1.1) |
| What do I build this week, and in what order? | [`tracemap-execution-plan.md`](./tracemap-execution-plan.md) — situation analysis, 12-wave schedule, deep specs (v2.1) |
| What's the status of any feature? | [`tracemap-feature-tracker.csv`](./tracemap-feature-tracker.csv) — one row per feature |

## Status snapshot (2026-10-01)

- **Stage:** planning complete (v1.1 features / v2.1 execution). **Code: none yet.**
- **Next action:** execution plan Part III, **Wave 0** (2 days: scaffold, CI, deploys, name/domain lock, competitive scan), then **Wave 1** — the walking skeleton that tests the core parsing bet.
- **Key schedule fact:** solo full-time ≈ wk 10 to public soft launch (Gate A1), ≈ wk 16 to Show HN (Gate B) + 2-wk buffer. Original "4-week MVP" claim is retired (execution plan §1.2).
- **Open decision:** product name/domain availability — Wave 0 exit gate, not yet checked.

## Tracker columns

`id` (permanent `F{phase}.{nn}`) · `feature` · `epic` · `phase` · `priority` (P0–P3) · `effort` (XS–XL) · `depends_on` · `status` (⬜🟡🔵✅🚫❌) · `wave` (the wave where the feature's **full acceptance criteria** pass; minimal-first slices land earlier and stay 🟡 — see feature plan §0.4).

Waves W0–W11 + `P1-tail` cover everything through launch; `Gate C+` (Phase 2) and `Gate D+` (Phase 3) features are blocked behind their metric gates.
