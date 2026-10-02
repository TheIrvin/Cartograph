package com.cartograph.ingestion.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cartograph.graph.model.RepositoryRef;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
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
        return get("/repos/" + ref.owner() + "/" + ref.repository(), RepositoryDto.class, etag, properties.maxResponseBytes());
    }

    public Response<CommitDto> commit(RepositoryRef ref, String requestedRef) {
        return get("/repos/" + ref.owner() + "/" + ref.repository() + "/commits/" + encodePath(requestedRef), CommitDto.class, null, properties.maxResponseBytes());
    }

    public Response<TreeDto> tree(RepositoryRef ref, String sha) {
        return get("/repos/" + ref.owner() + "/" + ref.repository() + "/git/trees/" + encodePath(sha) + "?recursive=1", TreeDto.class, null, properties.maxResponseBytes());
    }

    public Response<ContentDto> content(RepositoryRef ref, String path, String sha, String etag) {
        long encodedContentLimit = Math.min(properties.maxResponseBytes(), encodedContentLimit(properties.limits().maxFileBytes()));
        return get("/repos/" + ref.owner() + "/" + ref.repository() + "/contents/" + encodePath(path) + "?ref=" + encodePath(sha), ContentDto.class, etag, encodedContentLimit);
    }

    private <T> Response<T> get(String path, Class<T> type, String etag, long responseLimit) {
        return http.get().uri(path).headers(headers -> {
            headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
            headers.set(HttpHeaders.USER_AGENT, "Cartograph");
            if (!properties.token().isBlank()) headers.setBearerAuth(properties.token());
            if (etag != null && !etag.isBlank()) headers.set(HttpHeaders.IF_NONE_MATCH, etag);
        }).exchange((request, response) -> {
            int status = response.getStatusCode().value();
            String responseEtag = response.getHeaders().getFirst(HttpHeaders.ETAG);
            if (status == 304) throw new GitHubFetchException(GitHubFetchException.Kind.NOT_MODIFIED, status, "GitHub response was not modified");
            if (status == 404) throw new GitHubFetchException(GitHubFetchException.Kind.NOT_FOUND, status, "GitHub resource was not found");
            if (status == 429 || isRateLimited403(status, response.getHeaders())) {
                throw new GitHubFetchException(GitHubFetchException.Kind.RATE_LIMITED, status, "GitHub API rate limit exceeded");
            }
            if (status == 403) {
                throw new GitHubFetchException(GitHubFetchException.Kind.FORBIDDEN, status, "GitHub API permission denied");
            }
            if (status < 200 || status >= 300) throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, status, "GitHub API returned HTTP " + status);
            try {
                byte[] bytes = readBounded(response.getBody(), response.getHeaders().getContentLength(), responseLimit);
                return new Response<>(mapper.readValue(bytes, type), responseEtag);
            } catch (IOException | IllegalArgumentException exception) {
                throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, status, "Invalid GitHub response", exception);
            }
        });
    }

    private static long encodedContentLimit(long maxFileBytes) {
        try {
            return Math.addExact(Math.multiplyExact(Math.addExact(maxFileBytes, 2) / 3, 4), 4096);
        } catch (ArithmeticException exception) {
            return Long.MAX_VALUE;
        }
    }

    private static byte[] readBounded(InputStream body, long contentLength, long maxBytes) throws IOException {
        if (maxBytes <= 0) throw new IllegalArgumentException("Maximum response size must be positive");
        if (contentLength > maxBytes) throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, 413,
                "GitHub response exceeded the configured response limit");
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream((int) Math.min(maxBytes, 8192));
        byte[] buffer = new byte[8192];
        long total = 0;
        int read;
        while ((read = body.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, 413,
                    "GitHub response exceeded the configured response limit");
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static boolean isRateLimited403(int status, HttpHeaders headers) {
        if (status != 403) return false;
        return "0".equals(headers.getFirst("X-RateLimit-Remaining"))
                || headers.containsKey("Retry-After")
                || headers.containsKey("X-RateLimit-Reset");
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
        public byte[] decodedBytes() {
            if (!"base64".equalsIgnoreCase(encoding)) {
                throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, 200, "Unsupported GitHub content encoding");
            }
            try {
                byte[] bytes = Base64.getMimeDecoder().decode(content == null ? "" : content);
                // Decode strictly so replacement characters can never enter a snapshot.
                StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                        .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
                        .decode(java.nio.ByteBuffer.wrap(bytes));
                return bytes;
            } catch (java.io.IOException | IllegalArgumentException exception) {
                throw new GitHubFetchException(GitHubFetchException.Kind.UPSTREAM, 200, "Invalid UTF-8 GitHub content", exception);
            }
        }

        public String decoded() {
            return new String(decodedBytes(), StandardCharsets.UTF_8);
        }
    }
}
