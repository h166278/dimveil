# Contributing to Dim Veil

Thank you for contributing. Dim Veil is a privacy-first Android utility; changes to permissions, overlays, accessibility, Shizuku, or lifecycle behavior require extra care.

## Before you start

1. Read the [architecture](docs/architecture.md), [limitations](docs/limitations.md), [permissions](docs/permissions.md), and relevant [ADRs](docs/decisions/README.md).
2. Search existing issues before filing a new one.
3. Open an issue or discussion before large behavior, permission, or architecture changes.

## Development validation

Run the supported checks before opening a pull request:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug --stacktrace
```

For overlay-related changes, also complete the relevant physical-device checks in [docs/testing.md](docs/testing.md).

## Pull request requirements

Describe the motivation, behavior change, and test evidence. State explicitly whether the change affects any of the following:

- Manifest permissions or dependencies
- Privacy or local data storage
- Accessibility-service configuration or behavior
- Shizuku commands or authorization flow
- Window type, flags, alpha, touch-through policy, or overlay host routing
- Foreground-service lifecycle or notification behavior

UI changes should include redacted screenshots or recordings. Compatibility changes should name device, ROM, Android version, active host, and requested depth.

## Project boundaries

Contributions must not add network access, advertising, analytics, telemetry, screen/content reading, input monitoring, gesture injection, automated clicking, account systems, cloud sync, or attempts to bypass Android security policy without a public design discussion and maintainer approval.

## Style and commits

Keep changes focused. Update tests, documentation, and ADRs when the public behavior or a key decision changes. Use clear conventional-style commit subjects when practical, for example `fix(overlay): preserve touch-through opacity limit`.

## Conduct and security

By participating, follow the [Code of Conduct](CODE_OF_CONDUCT.md). Do not publish exploitable security details in a public issue; follow [SECURITY.md](SECURITY.md).
