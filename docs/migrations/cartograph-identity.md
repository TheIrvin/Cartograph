# Historical migration: TraceMap → Cartograph

The product and codebase now share the Cartograph identity. This is an early
development configuration/package rename; the indexing endpoint and graph JSON
contract are unchanged.

| Before | Now |
|---|---|
| `com.tracemap.TraceMapApplication` | `com.cartograph.CartographApplication` |
| Maven `com.tracemap:tracemap` | Maven `com.cartograph:cartograph` |
| `tracemap.*` properties | `cartograph.*` properties |
| `TRACEMAP_*` environment variables | `CARTOGRAPH_*` environment variables |
| `./data/tracemap.db` | `./data/cartograph.db` |
| `tracemap-*.md` and tracker CSV | `cartograph-*.md` and tracker CSV |

`GITHUB_TOKEN` keeps its existing name. Update IDE launch configurations,
Maven coordinates, Java imports, environment variables, and property overrides.
Old configuration prefixes are not aliases. Run `mvn clean verify` after updating
to remove compiled classes from the old package tree.

## Existing SQLite snapshots

No database file is deleted or automatically moved. The new default creates a
separate database. To keep using an existing database, explicitly select it:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--cartograph.sqlite.path=./data/tracemap.db"
```

Alternatively, stop the application, back up your database and any SQLite
sidecar files, and move the database using your normal SQLite backup procedure.
The schema does not change in this rename.

## Historical documentation

`.superpowers/`, `.zcode/plans/`, and `docs/superpowers/` preserve original
implementation reports and specifications. Old names there describe the past;
use the root README, AGENTS.md, and `cartograph-*` plans for current paths.
