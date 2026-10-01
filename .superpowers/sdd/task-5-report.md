# Task 5 report

Implemented the Tree-sitter JavaScript/TypeScript adapter and deterministic graph construction.

- Added maintained Maven Central Tree-sitter NG dependencies (`io.github.bonede` 0.26.6/grammar artifacts).
- Added `.js`, `.jsx`, `.ts`, and `.tsx` grammar selection, AST-backed definitions/imports/calls, warnings, exports, and golden fixture.
- Added repository/commit/path/kind/name/location SHA-256 node IDs and deterministic `GraphBuilder` ordering/remapping.
- Extended `ParsedFile` compatibly with warnings and exports while retaining the existing three-argument constructor.

Focused command (Java 17 Homebrew):

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@17 PATH="$JAVA_HOME/bin:$PATH" mvn -Dtest='com.tracemap.parsing.javascript.*Test,com.tracemap.graph.*Test' test
```

Full suite also passes with `mvn test` under the same Java 17 environment.

Concern: the adapter intentionally treats arrow-function definitions as unsupported and unresolved/dynamic calls as warnings; this is required by the task's no-invented-relationships rule.
