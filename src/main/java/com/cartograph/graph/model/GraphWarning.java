package com.cartograph.graph.model;

public record GraphWarning(String code, String message, String filePath, Integer line) {
    public GraphWarning {
        if (line != null && line < 1) {
            throw new IllegalArgumentException("Warning line must be positive");
        }
    }
}
