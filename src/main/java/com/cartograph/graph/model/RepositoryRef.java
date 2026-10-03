package com.cartograph.graph.model;

/** Normalized GitHub repository coordinate and requested branch, tag, or commit. */
public record RepositoryRef(String owner, String repository, String ref) {
    public String coordinate() {
        return owner + "/" + repository;
    }
}
