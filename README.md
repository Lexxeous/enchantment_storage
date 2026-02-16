# Enchantment Storage v1.0.0

```sh
.\gradlew --refresh-dependencies clean build runClient
```

## Clean Code Guidelines

1. **Nullability contracts:** Add `@Nullable`/`@NotNull` annotations consistently, and replace nullable returns with `Optional` only when it clearly improves call sites.
2. **Magic-number consolidation:** Move repeated GUI/layout and tuning values to named constants, and centralize slot indices, sync IDs, and NBT keys.
3. **Error/log hygiene:** Use structured, low-noise logs, and guard debug-only logs behind development checks.
4. **Side-safety boundaries:** Keep client-only APIs isolated to `src/client`, and prevent accidental client class loading from common code.
5. **Serialization hardening:** Version NBT payloads (for example with a `DataVersion` key), validate array/list bounds on read, and recover gracefully from mismatches.
6. **Registry/bootstrap discipline:** Use a single idempotent registration path, and avoid hidden static-initializer side effects unless intentional.
7. **Allocation/caching hotspots:** Reuse temporary collections in hot render/screen paths, and cache derived UI values per tick/frame when source data is unchanged.
8. **API encapsulation:** Avoid unnecessary public helper passthroughs (for example `addSlotInternal`), and keep utility classes domain-focused instead of generic dumping grounds.
9. **Pure logic extraction:** Pull deterministic math/state transforms out of handlers/screens into pure logic and utility classes to simplify unit-test coverage.
10. **Naming consistency:** Keep method names behavior-specific and standardize verb prefixes (`register`/`initialize`, `get`/`set`, `update`/`compute`, `is`/`has`/`can`, etc.).
11. **MC/mod-specific compat patterns:** Add feature/version guards where mappings or APIs churn, and minimize direct reliance on deprecated Fabric shims.
12. **Formatting/static analysis automation:** Add Checkstyle/Spotless/PMD/ErrorProne rules for import ordering/grouping, no wildcard imports, no trailing whitespace, and complexity thresholds.