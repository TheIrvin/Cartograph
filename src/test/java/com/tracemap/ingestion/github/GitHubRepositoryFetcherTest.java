package com.tracemap.ingestion.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracemap.graph.model.RepositoryRef;
import com.tracemap.ingestion.IndexingLimits;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitHubRepositoryFetcherTest {
    private HttpServer server;
    private AtomicInteger contentRequests;

    @BeforeEach
    void setUp() throws Exception {
        contentRequests = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String body;
            if (path.equals("/repos/acme/demo")) body = "{\"default_branch\":\"main\"}";
            else if (path.endsWith("/commits/main")) body = "{\"sha\":\"abc\"}";
            else if (path.endsWith("/git/trees/abc")) body = "{\"truncated\":false,\"tree\":[{\"path\":\"src/a.ts\",\"type\":\"blob\",\"size\":5},{\"path\":\"README.md\",\"type\":\"blob\",\"size\":999}]}";
            else { contentRequests.incrementAndGet(); body = "{\"encoding\":\"base64\",\"content\":\"Y29uc3QgYSA9IDE7\"}"; }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void tearDown() { server.stop(0); }

    @Test
    void inspectsTreeLimitsBeforeFetchingSupportedContent() {
        var props = new GitHubProperties("", URI.create("http://localhost:" + server.getAddress().getPort()), new IndexingLimits(1, 5, 5));
        var fetcher = new GitHubRepositoryFetcher(new GitHubClient(RestClient.builder(), new ObjectMapper(), props), props);
        var snapshot = fetcher.fetch(new RepositoryRef("acme", "demo", null));
        assertThat(snapshot.commitSha()).isEqualTo("abc");
        assertThat(snapshot.files()).singleElement().satisfies(file -> {
            assertThat(file.path()).isEqualTo("src/a.ts");
            assertThat(file.language()).isEqualTo("typescript");
            assertThat(file.content()).isEqualTo("const a = 1;");
        });
        assertThat(contentRequests).hasValue(1);
    }

    @Test
    void rejectsLimitsBeforeAnyContentRequest() {
        var props = new GitHubProperties("", URI.create("http://localhost:" + server.getAddress().getPort()), new IndexingLimits(1, 4, 5));
        var fetcher = new GitHubRepositoryFetcher(new GitHubClient(RestClient.builder(), new ObjectMapper(), props), props);
        assertThatThrownBy(() -> fetcher.fetch(new RepositoryRef("acme", "demo", null)))
                .isInstanceOf(com.tracemap.ingestion.RepositoryLimitException.class);
        assertThat(contentRequests).hasValue(0);
    }
}
