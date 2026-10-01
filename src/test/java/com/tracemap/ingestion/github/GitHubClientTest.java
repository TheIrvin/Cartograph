package com.tracemap.ingestion.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracemap.graph.model.RepositoryRef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitHubClientTest {
    private HttpServer server;
    private AtomicReference<String> authorization;
    private AtomicReference<String> ifNoneMatch;
    private GitHubClient client;

    @BeforeEach
    void setUp() throws Exception {
        authorization = new AtomicReference<>();
        ifNoneMatch = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            ifNoneMatch.set(exchange.getRequestHeaders().getFirst("If-None-Match"));
            String path = exchange.getRequestURI().getPath();
            if ("/repos/acme/demo".equals(path)) respond(exchange, 200, "{\"default_branch\":\"main\"}", "\"repo-v1\"");
            else if (path.endsWith("/commits/main")) respond(exchange, 200, "{\"sha\":\"abc123\"}", null);
            else if (path.endsWith("/git/trees/abc123")) respond(exchange, 200, "{\"truncated\":false,\"tree\":[]}", null);
            else if (path.endsWith("/contents/src/app.ts")) respond(exchange, 304, "", null);
            else if (path.endsWith("/missing")) respond(exchange, 404, "{}", null);
            else respond(exchange, 200, "{}", null);
        });
        server.start();
        GitHubProperties properties = new GitHubProperties("secret", URI.create("http://localhost:" + server.getAddress().getPort()),
                new com.tracemap.ingestion.IndexingLimits(10, 1000, 100));
        client = new GitHubClient(RestClient.builder(), new ObjectMapper(), properties);
    }

    @AfterEach
    void tearDown() { server.stop(0); }

    @Test
    void sendsBearerTokenAndAcceptsTypedResponses() {
        var response = client.repository(new RepositoryRef("acme", "demo", null), "\"old\"");
        assertThat(response.body().default_branch()).isEqualTo("main");
        assertThat(response.etag()).isEqualTo("\"repo-v1\"");
        assertThat(authorization).hasValue("Bearer secret");
        assertThat(ifNoneMatch).hasValue("\"old\"");
    }

    @Test
    void mapsNotModifiedAndNotFound() {
        assertThatThrownBy(() -> client.content(new RepositoryRef("acme", "demo", null), "src/app.ts", "abc", "\"old\""))
                .isInstanceOf(GitHubFetchException.class)
                .extracting("kind").isEqualTo(GitHubFetchException.Kind.NOT_MODIFIED);
        assertThatThrownBy(() -> client.repository(new RepositoryRef("acme", "missing", null)))
                .isInstanceOf(GitHubFetchException.class)
                .extracting("kind").isEqualTo(GitHubFetchException.Kind.NOT_FOUND);
    }

    @Test
    void omitsAuthorizationWhenTokenIsBlank() {
        GitHubProperties properties = new GitHubProperties("", URI.create("http://localhost:" + server.getAddress().getPort()),
                new com.tracemap.ingestion.IndexingLimits(10, 1000, 100));
        GitHubClient noToken = new GitHubClient(RestClient.builder(), new ObjectMapper(), properties);
        noToken.repository(new RepositoryRef("acme", "demo", null));
        assertThat(authorization).hasValue(null);
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body, String etag) throws java.io.IOException {
        if (etag != null) exchange.getResponseHeaders().set("ETag", etag);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, status == 304 ? -1 : bytes.length);
        if (status != 304) exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
