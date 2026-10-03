package com.cartograph.application;

import com.cartograph.graph.GraphBuilder;
import com.cartograph.graph.model.GraphSnapshot;
import com.cartograph.graph.model.RepositoryRef;
import com.cartograph.graph.model.RepositorySnapshot;
import com.cartograph.ingestion.GitHubUrlNormalizer;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

/** Cache-first application orchestration for repository indexing. */
@Service
public final class IndexRepositoryService {
    private final GitHubUrlNormalizer normalizer;
    private final RepositoryFetcher fetcher;
    private final GraphSnapshotRepository snapshots;
    private final GraphBuilder graphBuilder;

    /**
     * @param normalizer validates and converts repository URLs into references
     * @param fetcher resolves commits and obtains repository snapshots
     * @param snapshots stores and retrieves completed graph snapshots
     * @param graphBuilder builds a graph from a fetched repository snapshot
     */
    public IndexRepositoryService(GitHubUrlNormalizer normalizer, RepositoryFetcher fetcher,
            GraphSnapshotRepository snapshots, GraphBuilder graphBuilder) {
        this.normalizer = Objects.requireNonNull(normalizer);
        this.fetcher = Objects.requireNonNull(fetcher);
        this.snapshots = Objects.requireNonNull(snapshots);
        this.graphBuilder = Objects.requireNonNull(graphBuilder);
    }

    /**
     * Indexes a repository URL, reusing a cached graph for the resolved commit when available.
     *
     * @param repositoryUrl public repository URL to index
     * @return cached or newly built graph snapshot
     */
    public GraphSnapshot index(String repositoryUrl) {
        return index(normalizer.normalize(repositoryUrl));
    }

    /**
     * Indexes a normalized repository reference at its currently resolved commit.
     *
     * @param ref normalized owner, repository, and requested ref
     * @return cached or newly built graph snapshot
     */
    public GraphSnapshot index(RepositoryRef ref) {
        Objects.requireNonNull(ref, "repository reference");
        String commit = fetcher.resolveCommit(ref);
        return snapshots.find(ref.coordinate(), commit).orElseGet(() -> {
            RepositorySnapshot fetched = fetcher.fetchResolved(ref, commit);
            if (fetched == null || !Objects.equals(commit, fetched.commitSha())
                    || !ref.coordinate().equals(fetched.repository())) {
                throw new IllegalStateException("Fetched repository does not match the resolved commit");
            }
            GraphSnapshot built = graphBuilder.build(fetched);
            snapshots.save(built);
            return built;
        });
    }

    /** Returns the latest stored snapshot for {@code repository} without touching GitHub. */
    public Optional<GraphSnapshot> snapshot(String repository) {
        Objects.requireNonNull(repository, "repository");
        return snapshots.findLatest(repository);
    }
}
