package com.tracemap.graph.model;

public record GraphMetrics(int nodeCount, int edgeCount, int warningCount, int fileCount) {
    public GraphMetrics {
        if (nodeCount < 0 || edgeCount < 0 || warningCount < 0 || fileCount < 0) {
            throw new IllegalArgumentException("Graph metrics cannot be negative");
        }
    }
}
