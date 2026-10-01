package com.tracemap.graph.model;

public record GraphEdge(String fromId, String toId, EdgeKind kind, double confidence) {
    public GraphEdge {
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
    }
}
