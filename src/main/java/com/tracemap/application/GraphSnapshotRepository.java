package com.tracemap.application;

import java.util.Optional;

import com.tracemap.graph.model.GraphSnapshot;

public interface GraphSnapshotRepository {
    Optional<GraphSnapshot> find(String repository, String commitSha);

    void save(GraphSnapshot snapshot);
}
