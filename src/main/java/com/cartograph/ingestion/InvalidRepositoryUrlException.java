package com.cartograph.ingestion;

/** Indicates that a client-supplied repository URL cannot be normalized. */
public final class InvalidRepositoryUrlException extends IllegalArgumentException {
    public InvalidRepositoryUrlException(String message) {
        super(message);
    }

    public InvalidRepositoryUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
