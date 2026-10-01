package com.tracemap.graph;

import com.tracemap.application.SourceParser;
import com.tracemap.graph.model.*;
import java.util.*;

public final class GraphBuilder {
    private final SourceParser parser;
    public GraphBuilder(SourceParser parser) { this.parser = Objects.requireNonNull(parser); }

    public GraphSnapshot build(RepositorySnapshot repository) {
        List<ParsedFile> parsed = repository.files().stream().sorted(Comparator.comparing(SourceFile::path))
                .map(parser::parse).toList();
        Map<String, String> ids = new HashMap<>();
        List<GraphNode> nodes = parsed.stream().flatMap(p -> p.nodes().stream()).map(n -> {
            String id = StableNodeId.create(repository.repository(), repository.commitSha(), n.filePath(), n.kind(), n.name(), n.startLine(), 0);
            ids.put(n.stableId(), id);
            return new GraphNode(id, n.kind(), n.name(), n.filePath(), n.startLine(), n.endLine());
        }).sorted(Comparator.comparing(GraphNode::stableId)).toList();
        List<GraphEdge> edges = parsed.stream().flatMap(p -> p.edges().stream()).map(e -> new GraphEdge(
                        ids.getOrDefault(e.fromId(), e.fromId()), ids.getOrDefault(e.toId(), e.toId()), e.kind(), e.confidence()))
                .sorted(Comparator.comparing(GraphEdge::fromId).thenComparing(GraphEdge::toId).thenComparing(GraphEdge::kind)).toList();
        List<GraphWarning> warnings = parsed.stream().flatMap(p -> p.warnings().stream())
                .sorted(Comparator.comparing(GraphWarning::filePath).thenComparing(w -> Optional.ofNullable(w.line()).orElse(0)).thenComparing(GraphWarning::code)).toList();
        return new GraphSnapshot(repository.repository(), repository.commitSha(), nodes, edges, warnings,
                new GraphMetrics(nodes.size(), edges.size(), warnings.size(), repository.files().size()));
    }
}
