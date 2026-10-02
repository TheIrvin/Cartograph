package com.tracemap.graph.model;

public record RepositoryRef(String owner, String repository, String ref) {
    public String coordinate() {
        return owner + "/" + repository;
    }
}
