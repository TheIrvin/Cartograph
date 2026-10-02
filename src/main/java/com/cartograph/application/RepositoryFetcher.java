package com.cartograph.application;

import com.cartograph.graph.model.RepositoryRef;
import com.cartograph.graph.model.RepositorySnapshot;

@FunctionalInterface
public interface RepositoryFetcher {
    RepositorySnapshot fetch(RepositoryRef ref);

    /** Fetches the immutable commit already resolved by the caller, never the moving ref. */
    default RepositorySnapshot fetchResolved(RepositoryRef ref, String commitSha) {
        if (commitSha == null || commitSha.isBlank()) {
            throw new IllegalArgumentException("Resolved commit must not be blank");
        }
        RepositorySnapshot snapshot = fetch(new RepositoryRef(ref.owner(), ref.repository(), commitSha));
        if (snapshot == null || !commitSha.equals(snapshot.commitSha())
                || !ref.coordinate().equals(snapshot.repository())) {
            throw new IllegalStateException("Fetched repository does not match the resolved commit");
        }
        return snapshot;
    }

    /**
     * Resolves the requested branch/ref. Production adapters should avoid tree downloads;
     * this fallback preserves compatibility with simple offline fetch-only fixtures.
     */
    default String resolveCommit(RepositoryRef ref) {
        return fetch(ref).commitSha();
    }
}
