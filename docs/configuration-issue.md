# F0.05: document and verify the runtime configuration contract

## Outcome

A new contributor can configure local indexing without guessing which Spring property or environment variable is supported.

## Scope

This is a bounded contribution to F0.05, not completion of the full deployment/secrets-management feature. The `cartograph.*` configuration namespace is the committed identity (the TraceMap → Cartograph rename has landed); do not reintroduce legacy prefixes.

## Starting points

- `src/main/resources/application.yml`
- `src/main/java/com/cartograph/ingestion/github/GitHubProperties.java`
- `.env.example`
- `README.md`

## Acceptance criteria

- [ ] Document every supported GitHub limit, timeout, retry, and cache setting with units and defaults from the implementation.
- [ ] Show one working Maven application-argument example and one supported environment-variable example.
- [ ] Explain that `.env` files are not loaded automatically by Spring Boot.
- [ ] Add a focused Spring binding test that proves the documented names override defaults; cover invalid values where setters validate them.
- [ ] Never require or print a real GitHub token in tests.
- [ ] Run `mvn test` on Java 17 and report the result in the PR.

## Coordination

Tracked as issue #11. F0.08 resilience settings (retry, timeout, ETag cache) are merged on `main` — document those keys from `GitHubProperties` as part of this work rather than reproducing their implementation. Keep F0.05 In Progress until its full feature-card acceptance criteria are satisfied.
