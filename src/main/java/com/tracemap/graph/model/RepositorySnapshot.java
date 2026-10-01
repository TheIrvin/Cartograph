package com.tracemap.graph.model;

import java.util.List;

public record RepositorySnapshot(String repository, String commitSha, List<SourceFile> files) {
    public RepositorySnapshot {
        files = List.copyOf(files);
    }
}
