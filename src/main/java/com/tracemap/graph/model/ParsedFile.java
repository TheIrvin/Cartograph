package com.tracemap.graph.model;

import java.util.List;

public record ParsedFile(String path, List<GraphNode> nodes, List<GraphEdge> edges) {
    public ParsedFile {
        nodes = List.copyOf(nodes);
        edges = List.copyOf(edges);
    }
}
