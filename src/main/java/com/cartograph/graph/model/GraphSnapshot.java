package com.cartograph.graph.model;

import java.util.List;

/** Complete immutable graph and diagnostics for one repository commit. */
public record GraphSnapshot(
        String repository,
        String commitSha,
        List<GraphNode> nodes,
        List<GraphEdge> edges,
        List<GraphWarning> warnings,
        GraphMetrics metrics) {
    public GraphSnapshot {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
        warnings = List.copyOf(warnings);
    }
}
