package com.tracemap.ingestion.github;

import com.tracemap.ingestion.IndexingLimits;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

/** Configuration owned by the GitHub adapter; the token is never part of a request DTO. */
@ConfigurationProperties(prefix = "tracemap.github")
public class GitHubProperties {
    private String token = "";
    private URI baseUrl = URI.create("https://api.github.com");
    private long maxFiles = 10_000;
    private long maxTotalBytes = 1_073_741_824;
    private long maxFileBytes = 10_485_760;

    public GitHubProperties() { }

    public GitHubProperties(String token, URI baseUrl, IndexingLimits limits) {
        this.token = token == null ? "" : token;
        this.baseUrl = baseUrl == null ? URI.create("https://api.github.com") : baseUrl;
        this.maxFiles = limits.maxFileCount();
        this.maxTotalBytes = limits.maxTotalBytes();
        this.maxFileBytes = limits.maxFileBytes();
    }

    public String token() { return token; }
    public URI baseUrl() { return baseUrl; }
    public IndexingLimits limits() { return new IndexingLimits(maxFiles, maxTotalBytes, maxFileBytes); }
    public void setToken(String token) { this.token = token == null ? "" : token; }
    public void setBaseUrl(URI baseUrl) { this.baseUrl = baseUrl; }
    public void setMaxFiles(long value) { this.maxFiles = value; }
    public void setMaxTotalBytes(long value) { this.maxTotalBytes = value; }
    public void setMaxFileBytes(long value) { this.maxFileBytes = value; }
}
