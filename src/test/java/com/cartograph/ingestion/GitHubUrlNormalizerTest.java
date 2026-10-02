package com.cartograph.ingestion;

import com.cartograph.graph.model.RepositoryRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GitHubUrlNormalizerTest {
    private final GitHubUrlNormalizer normalizer = new GitHubUrlNormalizer();

    @Test
    void normalizesRepositoryUrlsAndDropsQueryAndFragment() {
        assertEquals(new RepositoryRef("octocat", "hello-world", null),
                normalizer.normalize("https://github.com/octocat/Hello-World/?tab=readme#content"));
    }

    @Test
    void preservesTreeRefIncludingSlash() {
        assertEquals(new RepositoryRef("octocat", "hello-world", "feature/one"),
                normalizer.normalize("https://github.com/octocat/hello-world/tree/feature/one"));
    }

    @Test
    void acceptsTrailingSlashOnTreeRef() {
        assertEquals(new RepositoryRef("octocat", "hello-world", "main"),
                normalizer.normalize("https://github.com/octocat/hello-world/tree/main/"));
    }

    @Test
    void rejectsExtraTrailingSlashes() {
        assertThrows(IllegalArgumentException.class,
                () -> normalizer.normalize("https://github.com/octocat/hello-world////"));
        assertThrows(IllegalArgumentException.class,
                () -> normalizer.normalize("https://github.com/octocat/hello-world/tree/main////"));
    }

    @Test
    void rejectsMalformedOrUnsafeUrls() {
        String[] invalid = {
                "http://github.com/octocat/hello-world",
                "https://gist.github.com/octocat/hello-world",
                "https://github.com/octocat",
                "https://github.com//hello-world",
                "https://github.com/octocat/hello-world/blob/main/README.md",
                "https://github.com/octocat/hello-world/tree/main/../../secret",
                "https://github.com/octocat/hello-world/tree/%2e%2e/secret",
                "https://github.com/octocat/hello-world/tree/main%2F..%2Fsecret",
                "https://github.com/octocat/hello-world/tree/"
                ,"https://github.com/octocat/.git"
                ,"https://github.com/octocat/.git.git"
        };
        for (String url : invalid) {
            assertThrows(IllegalArgumentException.class, () -> normalizer.normalize(url), url);
        }
    }
}
