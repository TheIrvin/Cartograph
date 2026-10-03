/**
 * REST adapter for Cartograph: {@code IndexController} exposes the indexing
 * use case, request/response records carry the wire shape, and
 * {@code ApiExceptionHandler} returns a stable {@code {code, message}} response
 * for handled request, upstream, and unexpected exceptions.
 *
 * <p>Boundary rules: no business logic here — this package delegates to the
 * {@code com.cartograph.application} use case and must not import GitHub
 * clients, parsers, or persistence types.
 */
package com.cartograph.api;
