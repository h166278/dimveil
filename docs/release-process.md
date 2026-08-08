# Release process

This document defines the minimum release checklist for maintainers.

## Prepare

1. Confirm the target version in `app/build.gradle.kts`.
2. Update `CHANGELOG.md` with user-visible changes, security notes, and known limitations.
3. Review every manifest permission, accessibility configuration, Shizuku command, window flag/type/alpha change, foreground-service behavior, dependency update, and DataStore schema change.
4. Update relevant ADRs and public documentation when behavior or a decision changes.

## Validate

1. Run `./gradlew testDebugUnitTest lintDebug assembleDebug --stacktrace`.
2. Ensure GitHub Actions is green.
3. Complete the manual regression matrix in [testing.md](testing.md) on available physical devices.
4. Verify release notes do not promise unsupported coverage or behavior.

## Publish

1. Create an annotated Git tag such as `v1.0.0`.
2. Create a GitHub Release from that tag.
3. Attach the release APK and publish its SHA-256 checksum.
4. State the version, minimum Android version, key changes, upgrade notes, and known limitations.
5. Distinguish release APKs from temporary CI debug artifacts.

## After release

Monitor reproducible reports, update the compatibility matrix only with evidence, and prepare a patch release for confirmed regressions or security issues.
