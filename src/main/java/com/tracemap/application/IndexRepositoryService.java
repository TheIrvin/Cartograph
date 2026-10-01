package com.tracemap.application;

import com.tracemap.graph.model.GraphSnapshot;
import com.tracemap.graph.model.RepositoryRef;

@FunctionalInterface
public interface IndexRepositoryService {
    GraphSnapshot index(RepositoryRef ref);
}
