package com.tracemap.application;

import com.tracemap.graph.GraphBuilder;
import com.tracemap.graph.model.GraphSnapshot;
import com.tracemap.graph.model.RepositoryRef;
import com.tracemap.graph.model.RepositorySnapshot;
import com.tracemap.ingestion.GitHubUrlNormalizer;
import org.springframework.stereotype.Service;

import java.util.Objects;

/** Cache-first application orchestration for repository indexing. */
@Service
public final class IndexRepositoryService {
    private final GitHubUrlNormalizer normalizer;
    private final RepositoryFetcher fetcher;
    private final GraphSnapshotRepository snapshots;
    private final GraphBuilder graphBuilder;

    public IndexRepositoryService(GitHubUrlNormalizer normalizer, RepositoryFetcher fetcher,
            GraphSnapshotRepository snapshots, GraphBuilder graphBuilder) {
        this.normalizer = Objects.requireNonNull(normalizer);
        this.fetcher = Objects.requireNonNull(fetcher);
        this.snapshots = Objects.requireNonNull(snapshots);
        this.graphBuilder = Objects.requireNonNull(graphBuilder);
    }

    public GraphSnapshot index(String repositoryUrl) {
        return index(normalizer.normalize(repositoryUrl));
    }

    public GraphSnapshot index(RepositoryRef ref) {
        Objects.requireNonNull(ref, "repository reference");
        String commit = fetcher.resolveCommit(ref);
        return snapshots.find(ref.coordinate(), commit).orElseGet(() -> {
            RepositorySnapshot fetched = fetcher.fetch(ref);
            GraphSnapshot built = graphBuilder.build(fetched);
            snapshots.save(built);
            return built;
        });
    }
}
