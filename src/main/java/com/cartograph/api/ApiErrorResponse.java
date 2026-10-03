package com.cartograph.api;

/**
 * Stable JSON error body returned by the HTTP API.
 *
 * @param code machine-readable error identifier
 * @param message safe, human-readable explanation that does not expose internal details
 */
public record ApiErrorResponse(String code, String message) { }
