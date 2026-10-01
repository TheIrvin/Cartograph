package com.tracemap.graph.model;

public record SourceLocation(String filePath, int startLine, int startColumn, int endLine, int endColumn) {
    public SourceLocation {
        if (startLine < 1 || endLine < startLine || startColumn < 0 || endColumn < 0) {
            throw new IllegalArgumentException("Source location must be ordered and non-negative");
        }
    }
}
