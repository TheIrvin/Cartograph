package com.cartograph.application;

import com.cartograph.graph.GraphBuilder;
import com.cartograph.graph.model.*;
import com.cartograph.ingestion.GitHubUrlNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class IndexRepositoryServiceTest {
    private final RepositoryRef ref = new RepositoryRef("acme", "widgets", "main");
    private final GraphSnapshot cached = snapshot("sha");

    @Test
    void cacheHitDoesNotFetchParseBuildOrSave() {
        RepositoryFetcher fetcher = mock(RepositoryFetcher.class);
        GraphSnapshotRepository repository = mock(GraphSnapshotRepository.class);
        when(fetcher.resolveCommit(ref)).thenReturn("sha");
        when(repository.find("acme/widgets", "sha")).thenReturn(Optional.of(cached));
        IndexRepositoryService service = new IndexRepositoryService(new GitHubUrlNormalizer(), fetcher, repository,
                new GraphBuilder(file -> { throw new AssertionError("parser must not run"); }));

        assertSame(cached, service.index(ref));
        verify(fetcher, never()).fetch(any());
        verify(repository, never()).save(any());
    }

    @Test
    void missFetchesBuildsAndSavesSnapshot() {
        RepositoryFetcher fetcher = mock(RepositoryFetcher.class);
        GraphSnapshotRepository repository = mock(GraphSnapshotRepository.class);
        RepositorySnapshot source = new RepositorySnapshot("acme/widgets", "sha", List.of(new SourceFile("a.ts", "", "typescript")));
        when(fetcher.resolveCommit(ref)).thenReturn("sha");
        when(repository.find("acme/widgets", "sha")).thenReturn(Optional.empty());
        when(fetcher.fetch(ref)).thenReturn(source);
        IndexRepositoryService service = new IndexRepositoryService(new GitHubUrlNormalizer(), fetcher, repository,
                new GraphBuilder(file -> new ParsedFile(file.path(), List.of(), List.of())));

        GraphSnapshot built = service.index(ref);
        var order = inOrder(fetcher, repository);
        order.verify(fetcher).resolveCommit(ref);
        order.verify(repository).find("acme/widgets", "sha");
        order.verify(fetcher).fetch(ref);
        order.verify(repository).save(built);
    }

    private static GraphSnapshot snapshot(String sha) {
        return new GraphSnapshot("acme/widgets", sha, List.of(), List.of(), List.of(), new GraphMetrics(0, 0, 0, 0));
    }
}
