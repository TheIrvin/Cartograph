package com.tracemap.application;

import com.tracemap.graph.model.RepositoryRef;
import com.tracemap.graph.model.RepositorySnapshot;

@FunctionalInterface
public interface RepositoryFetcher {
    RepositorySnapshot fetch(RepositoryRef ref);
}
