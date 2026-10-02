package com.cartograph.application;

import com.cartograph.graph.model.RepositoryRef;
import com.cartograph.graph.model.RepositorySnapshot;

@FunctionalInterface
public interface RepositoryFetcher {
    RepositorySnapshot fetch(RepositoryRef ref);

    /** Resolves the requested branch/ref without downloading the repository tree. */
    default String resolveCommit(RepositoryRef ref) {
        return fetch(ref).commitSha();
    }
}
