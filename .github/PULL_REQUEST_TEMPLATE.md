## What does this PR do?

<!-- One or two sentences: what changes and why. Link the issue: Closes #123 -->

## Which feature / wave?

<!-- Reference the tracker ID if applicable, e.g. F0.17 (Python extractor, W3) -->

## Checklist

- [ ] `mvn test` passes locally
- [ ] Tests added/updated for the change
- [ ] Hexagonal boundaries respected (no framework imports inside `application`)
- [ ] Error responses keep the stable `{ code, message }` contract
- [ ] Docs + `cartograph-feature-tracker.csv` updated if a feature's acceptance criteria are now fully met
