package com.tracemap.graph.model;

public record GraphNode(String stableId, SymbolKind kind, String name, String filePath, int startLine, int endLine) {
    public GraphNode {
        if (startLine < 1 || endLine < startLine) {
            throw new IllegalArgumentException("Source location must have positive, ordered lines");
        }
    }
}
