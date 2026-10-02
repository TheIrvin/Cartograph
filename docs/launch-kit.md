# Share Cartograph

## One sentence

Cartograph turns GitHub JavaScript and TypeScript source into inspectable graph snapshots with tree-sitter, Java, and SQLite.

## Short announcement

> Exploring an unfamiliar codebase? I'm building Cartograph: a source-derived code graph backend. Give it a GitHub URL and it returns symbols, relationships, locations, and analysis warnings as JSON. It runs locally on Java 17 with embedded SQLite. The interactive graph viewer is on the roadmap, and we're looking for contributors in parsing, graph visualization, and developer tooling.
>
> Try it, bring a tiny parser edge case, or pick an issue: https://github.com/pacman-cli/Cartograph

## A 60-second demo

1. Show the README's architecture diagram and explain the problem: navigating an unfamiliar repository.
2. Start the API using the README's Java 17/Maven commands.
3. POST a small public JS/TS repository URL to `/api/v1/index`.
4. Show one returned symbol's source location, a relationship, and a warning. Explain that static extraction is not complete runtime tracing.
5. Repeat the request for the same commit and explain the SQLite snapshot cache. Avoid unmeasured speed claims.
6. End on a scoped contributor issue and the contribution guide.

Use your own measured output in screenshots. Do not present a concept diagram as an implemented graph-viewer screenshot. Remove credentials and private repository information before recording.

## Distribution

- Share a concrete demo in relevant Java, static-analysis, and developer-tools communities where project sharing is welcome.
- Ask for a specific kind of feedback: "Which JS/TS construct should we test next?"
- Publish progress when a runnable capability lands, with a linked PR and reproducible example.
- Thank contributors by linking their work, and invite useful bug reports as well as code.
- Prefer a few relevant conversations over repeated cross-posts or unsolicited messages.

Measure useful signals: successful first runs, reproducible reports, reviewed contributions, and returning contributors. Stars can help discovery; they do not establish correctness or guarantee adoption.
