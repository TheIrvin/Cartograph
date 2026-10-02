package com.cartograph.application;

import java.util.Optional;

import com.cartograph.graph.model.GraphSnapshot;

public interface GraphSnapshotRepository {
    Optional<GraphSnapshot> find(String repository, String commitSha);

    void save(GraphSnapshot snapshot);
}
