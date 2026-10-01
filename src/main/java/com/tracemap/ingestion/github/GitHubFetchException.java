package com.tracemap.ingestion.github;

public class GitHubFetchException extends RuntimeException {
    public enum Kind { NOT_FOUND, FORBIDDEN, RATE_LIMITED, NOT_MODIFIED, UPSTREAM }

    private final Kind kind;
    private final int status;

    public GitHubFetchException(Kind kind, int status, String message) {
        super(message);
        this.kind = kind;
        this.status = status;
    }

    public GitHubFetchException(Kind kind, int status, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.status = status;
    }

    public Kind kind() { return kind; }
    public int status() { return status; }
}
