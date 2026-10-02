# Cartograph architecture diagrams

Two complementary views of the backend, both authored with the Archify skill
and pinned to a commit so every source reference stays verifiable.

## The diagrams

| View | Interactive HTML | Editable spec | Embedded in README as |
|---|---|---|---|
| Structure — ports & adapters | [cartograph-architecture.html](cartograph-architecture.html) | [cartograph-architecture.json](cartograph-architecture.json) | `docs/assets/architecture-live.svg` (animated) |
| Behavior — request & cache flow | [cartograph-cache-flow.html](cartograph-cache-flow.html) | [cartograph-cache-flow.json](cartograph-cache-flow.json) | `docs/assets/cache-flow-live.svg` (animated) |

The README embeds are hand-authored animated SVG renditions of these two
diagrams (CSS keyframes only, no scripts, `prefers-reduced-motion` aware), kept
in visual sync with the specs above. If you change a spec's topology or
labels, update the matching `-live.svg` in the same PR.

Download an HTML file and open it in a browser: the self-contained viewer
supports pan/zoom, light/dark themes, source-linked evidence badges, image
export, and the opt-in `trace` animation (`meta.animation: "trace"` in each
spec) that plays relationship flow through the diagram. GitHub displays HTML
as source rather than executing it. The README's own banner and pipeline SVGs
are separately hand-animated with CSS keyframes (no scripts), so they animate
inline on GitHub while honoring `prefers-reduced-motion`.

## Scope and evidence

Both diagrams are pinned to commit
`a51a4eb51365422f95d3433c50edf350820a4d28` (the TraceMap → Cartograph rename
plus the committed F0.08 resilience work). They depict modules inside one
Spring Boot service, not independent microservices. SQLite is the persistent
graph store; the GitHub adapter retrieves source files; tree-sitter performs
static extraction. The REST response is JSON. A graphical product viewer
remains planned (F0.25+).

Static analysis has incomplete coverage; consult the warnings returned with
each snapshot.

## Regeneration

With the Archify skill installed, from the repository root:

```bash
node /path/to/archify/bin/archify.mjs finalize architecture \
  docs/diagrams/cartograph-architecture.json \
  docs/diagrams/cartograph-architecture.html \
  --repo-root . --quality showcase --out-dir .archify/arch-review --json

node /path/to/archify/bin/archify.mjs finalize architecture \
  docs/diagrams/cartograph-cache-flow.json \
  docs/diagrams/cartograph-cache-flow.html \
  --repo-root . --quality showcase --out-dir .archify/cache-flow-review --json
```

When the pinned revision moves, update `meta.repository.revision`, inspect the
changed source ranges cited in `sources`, adjust the coordinates if the scene
grew, and regenerate both diagrams together. Use a fresh `--out-dir` for
evidence on every rerun.

The generated receipts report schema, delivery, artifact, and real-browser
checks. They are local build evidence, not hand-authored documentation.
Automated checks passed for both versions: containment, readability, and
theme checks, with non-overlapping routes. Use the viewer's export menu to
create fresh images after opening the HTML locally, and keep the JSON
specification with any revised HTML so contributors can reproduce it.
