package com.tracemap.ingestion.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tracemap.graph.model.RepositoryRef;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/** Small typed boundary around the GitHub REST API. DTOs intentionally stay adapter-local. */
public class GitHubClient {
    private final RestClient http;
    private final ObjectMapper mapper;
    private final GitHubProperties properties;

    public GitHubClient(RestClient.Builder builder, ObjectMapper mapper, GitHubProperties properties) {
        this.mapper = mapper;
        this.properties = properties;
        this.http = builder.baseUrl(properties.baseUrl().toString()).build();
    }

    public GitHubClient(RestClient http, ObjectMapper mapper, GitHubProperties properties) {
        this.http = http;
        this.mapper = mapper;
        this.properties = properties;
    }

    public Response<RepositoryDto> repository(RepositoryRef ref) { return repository(ref, null); }
    public Response<RepositoryDto> repository(RepositoryRef ref, String etag) {
        return get("/repos/" + ref.owner() + "/" + ref.repository(), RepositoryDto.class, etag);
    }

    public Response<CommitDto> commit(RepositoryRef ref, String requestedRef) {
        return get("/repos/" + ref.owner() + "/" + ref.repository() + "/commits/" + encodePath(requestedRef), CommitDto.class, null);
    }

    public Response<TreeDto> tree(RepositoryRef ref, String sha) {
        return get("/repos/" + ref.owner() + "/" + ref.repository() + "/git/trees/" + encodePath(sha) + "?recursive=1", TreeDto.class, null);
    }

    public Response<ContentDto> content(RepositoryRef ref, String path, String sha, String etag) {
        return get("/repos/" + ref.owner() + "/" + ref.repository() + "/contents/" + encodePath(path) + "?ref=" + encodePath(sha), ContentDto.class, etag);
    }

    private <T> Response<T> get(String path, Class<T> type, String etag) {
        return http.get().uri(path).headers(headers -> {
            headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
            headers.set(HttpHeaders.USER_AGENT, "TraceMap");
            if (!properties.token().isBlank()) headers.setBearerAuth(properties.token());
            if (etag != null && !etag.isBlank()) headers.set(HttpHeaders.IF_NONE_MATCH, etag);
        }).exchange((request, response) -> {
            int status = response.getStatusCode().value();
            String responseEtag = response.getHeaders().getFirst(HttpHeaders.ETAG);
            if (status == 304) throw new GitHubFetchException(GitHubFetchException.Kind.NOT_MODIFIED, status, "GitHub response was not modified");
            if (status == 404) throw new GitHubFetchException(GitHubFetchException.Kind.NOT_FOUND, status, "GitHub resource was not found");
            if (status == 403 || status == 429 || (status >= 400 && response.getHeaders().containsKey("X-RateLimit-Remaining")
                    && "0".equals(response.getHeaders().getFirst("X-RateLimit-Remaining")))) {
                throw new GitHubFetchException(GitHubFetchException.Kind.RATE_LIMITED, status, "GitHub API rate limit exceeded");
            }
            if (status < 200 || status >= 300) throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, status, "GitHub API returned HTTP " + status);
            try {
                byte[] bytes = response.getBody().readAllBytes();
                return new Response<>(mapper.readValue(bytes, type), responseEtag);
            } catch (IOException exception) {
                throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, status, "Invalid GitHub response", exception);
            }
        });
    }

    private static String encodePath(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20").replace("%2F", "/");
    }

    public record Response<T>(T body, String etag) { }
    public record RepositoryDto(String default_branch) { }
    public record CommitDto(String sha) { }
    public record TreeDto(Boolean truncated, List<TreeEntry> tree) { }
    public record TreeEntry(String path, String type, Long size, String sha) { }
    public record ContentDto(String type, String encoding, String content, Long size) {
        public String decoded() {
            if (!"base64".equalsIgnoreCase(encoding)) throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, 200, "Unsupported GitHub content encoding");
            return new String(Base64.getMimeDecoder().decode(content == null ? "" : content), StandardCharsets.UTF_8);
        }
    }
}
