package com.tracemap.graph.model;

import java.util.List;

public record ParsedFile(String path, List<GraphNode> nodes, List<GraphEdge> edges,
        List<GraphWarning> warnings, List<String> exports) {
    public ParsedFile(String path, List<GraphNode> nodes, List<GraphEdge> edges) {
        this(path, nodes, edges, List.of(), List.of());
    }
    public ParsedFile {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
        warnings = List.copyOf(warnings);
        exports = List.copyOf(exports);
    }
}
