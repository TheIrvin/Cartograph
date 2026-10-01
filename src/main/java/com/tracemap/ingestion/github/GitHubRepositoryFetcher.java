package com.tracemap.ingestion.github;

import com.tracemap.application.RepositoryFetcher;
import com.tracemap.graph.model.RepositoryRef;
import com.tracemap.graph.model.RepositorySnapshot;
import com.tracemap.graph.model.SourceFile;
import com.tracemap.ingestion.IndexingLimits;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GitHubRepositoryFetcher implements RepositoryFetcher {
    private final GitHubClient client;
    private final IndexingLimits limits;

    public GitHubRepositoryFetcher(GitHubClient client, IndexingLimits limits) {
        this.client = client;
        this.limits = limits;
    }

    public GitHubRepositoryFetcher(GitHubClient client, GitHubProperties properties) {
        this(client, properties.limits());
    }

    @Override
    public String resolveCommit(RepositoryRef ref) {
        String requested = ref.ref();
        if (requested == null || requested.isBlank()) {
            requested = client.repository(ref).body().default_branch();
        }
        return client.commit(ref, requested).body().sha();
    }

    @Override
    public RepositorySnapshot fetch(RepositoryRef ref) {
        GitHubClient.RepositoryDto metadata = client.repository(ref).body();
        String requested = ref.ref() == null ? metadata.default_branch() : ref.ref();
        String sha = client.commit(ref, requested).body().sha();
        GitHubClient.TreeDto tree = client.tree(ref, sha).body();
        if (Boolean.TRUE.equals(tree.truncated())) throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, 200, "GitHub tree response was truncated");

        List<GitHubClient.TreeEntry> candidates = tree.tree().stream()
                .filter(entry -> "blob".equals(entry.type()) && supported(entry.path()))
                .toList();
        limits.validateFetchedTree(candidates.stream().map(entry -> entry.size() == null ? 0L : entry.size()).toList());
        List<SourceFile> files = new ArrayList<>(candidates.size());
        long downloadedBytes = 0;
        for (GitHubClient.TreeEntry entry : candidates) {
            byte[] contentBytes = client.content(ref, entry.path(), sha, null).body().decodedBytes();
            downloadedBytes = limits.validateFetchedContent(contentBytes.length, downloadedBytes);
            files.add(new SourceFile(entry.path(), new String(contentBytes, java.nio.charset.StandardCharsets.UTF_8), language(entry.path())));
        }
        return new RepositorySnapshot(ref.coordinate(), sha, files);
    }

    private static boolean supported(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return lower.endsWith(".js") || lower.endsWith(".jsx") || lower.endsWith(".ts") || lower.endsWith(".tsx");
    }

    private static String language(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        return (lower.endsWith(".ts") || lower.endsWith(".tsx")) ? "typescript" : "javascript";
    }
}
