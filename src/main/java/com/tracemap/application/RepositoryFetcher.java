package com.tracemap.application;

import com.tracemap.graph.model.RepositoryRef;
import com.tracemap.graph.model.RepositorySnapshot;

@FunctionalInterface
public interface RepositoryFetcher {
    RepositorySnapshot fetch(RepositoryRef ref);

    /** Resolves the requested branch/ref without downloading the repository tree. */
    default String resolveCommit(RepositoryRef ref) {
        return fetch(ref).commitSha();
    }
}
