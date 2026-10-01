package com.tracemap.graph;

import static org.junit.jupiter.api.Assertions.*;

import com.tracemap.graph.model.*;
import com.tracemap.parsing.javascript.JavaScriptTypeScriptParser;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class GraphBuilderTest {
    @Test
    void buildsGoldenGraphWithScopedIdsWarningsAndMetrics() throws Exception {
        String source;
        try (var in = getClass().getResourceAsStream("/fixtures/simple-ts-repo/main.ts")) {
            source = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        var repo = new RepositorySnapshot("acme/demo", "abc123", List.of(new SourceFile("main.ts", source, "typescript")));
        var builder = new GraphBuilder(new JavaScriptTypeScriptParser());
        GraphSnapshot graph = builder.build(repo);
        assertEquals(graph, builder.build(repo));
        assertEquals(new GraphMetrics(6, 7, 1, 1), graph.metrics());
        Map<String, String> names = graph.nodes().stream().collect(Collectors.toMap(GraphNode::stableId, GraphNode::name));
        assertEquals(List.of("Greeter.say -> greet", "run -> greet"), graph.edges().stream()
                .filter(e -> e.kind() == EdgeKind.CALLS).map(e -> names.get(e.fromId()) + " -> " + names.get(e.toId())).sorted().toList());
        assertTrue(graph.edges().stream().allMatch(e -> names.containsKey(e.fromId()) && names.containsKey(e.toId())));
        var changed = builder.build(new RepositorySnapshot("acme/demo", "other", repo.files()));
        assertTrue(changed.nodes().stream().noneMatch(n -> names.containsKey(n.stableId())));
    }

    @Test
    void stableIdentityIncludesAllInputsAndNormalizesPaths() {
        String id = StableNodeId.create("acme/demo", "abc123", "src/./a.ts", SymbolKind.FUNCTION, "run", 3, 1);
        assertEquals(id, StableNodeId.create("acme/demo", "abc123", "src\\a.ts", SymbolKind.FUNCTION, "run", 3, 1));
        assertNotEquals(id, StableNodeId.create("acme/other", "abc123", "src/a.ts", SymbolKind.FUNCTION, "run", 3, 1));
        assertNotEquals(id, StableNodeId.create("acme/demo", "next", "src/a.ts", SymbolKind.FUNCTION, "run", 3, 1));
        assertNotEquals(id, StableNodeId.create("acme/demo", "abc123", "src/b.ts", SymbolKind.FUNCTION, "run", 3, 1));
        assertNotEquals(id, StableNodeId.create("acme/demo", "abc123", "src/a.ts", SymbolKind.METHOD, "run", 3, 1));
        assertNotEquals(id, StableNodeId.create("acme/demo", "abc123", "src/a.ts", SymbolKind.FUNCTION, "other", 3, 1));
        assertNotEquals(id, StableNodeId.create("acme/demo", "abc123", "src/a.ts", SymbolKind.FUNCTION, "run", 4, 1));
        assertNotEquals(id, StableNodeId.create("acme/demo", "abc123", "src/a.ts", SymbolKind.FUNCTION, "run", 3, 2));
    }

    @Test
    void repositoryFileOrderDoesNotChangeGraphAndCallsNeverResolveAcrossFiles() {
        var a = new SourceFile("a.ts", "function target() {}", "typescript");
        var b = new SourceFile("b.ts", "function run() { target(); }", "typescript");
        var builder = new GraphBuilder(new JavaScriptTypeScriptParser());
        var graph = builder.build(new RepositorySnapshot("repo", "sha", List.of(a, b)));
        assertEquals(graph, builder.build(new RepositorySnapshot("repo", "sha", List.of(b, a))));
        assertTrue(graph.edges().stream().noneMatch(e -> e.kind() == EdgeKind.CALLS));
        assertEquals(1, graph.warnings().size());
    }
}
