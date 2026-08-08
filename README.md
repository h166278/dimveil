# Dim Veil / 暗幕

[![Android CI](https://github.com/h166278/dimveil/actions/workflows/android.yml/badge.svg)](https://github.com/h166278/dimveil/actions/workflows/android.yml)
[![License: GPL-3.0-or-later](https://img.shields.io/badge/License-GPL--3.0--or--later-blue.svg)](LICENSE)

**Dim Veil** is a privacy-first Android dim overlay that visually goes beyond the system minimum brightness without changing the system brightness. It renders a pure-black, touch-through layer locally on your device.

> The app is called **暗幕** in Chinese Android system UI.

## What it does

- Adds a local pure-black visual overlay with selectable modes and 0–90% requested dim depth.
- Keeps touch input with the app underneath whenever Android permits touch-through overlays.
- Uses a normal application overlay by default.
- Optionally uses an accessibility-overlay window to improve coverage on some devices and system areas.
- Stores only local display preferences (mode and dim depth).

## What it never does

- Does not change system brightness or write `Settings.System` brightness values.
- Has no Internet permission, ads, usage measurement, accounts, cloud sync, tracking, or telemetry.
- Does not read screen contents, accessibility nodes, text input, notifications, or other app data.
- Does not inject gestures, click controls, automate other apps, or monitor user input.

## Install

For development builds, open the **Android CI** workflow and download its `dimveil-debug-apk` artifact after a successful run. CI artifacts are temporary test builds, not stable releases.

When releases are published, download APKs only from this repository's [Releases](https://github.com/h166278/dimveil/releases) page and verify the published SHA-256 checksum.

## Quick start

1. Open Dim Veil and grant the **display over other apps** permission.
2. Choose a mode and dim depth, then start the overlay.
3. For deeper or more complete coverage, optionally enable **暗幕全屏覆盖服务** in Android Accessibility settings.
4. Stop the overlay from Dim Veil. Removing Dim Veil from Recents also stops it intentionally.

## Overlay paths

| Path | When used | Permission | Strength | Important limit |
|---|---|---|---|---|
| Normal application overlay | Default fallback | Display over other apps | Lowest-friction path | May not cover all system surfaces; Android 12+ constrains touch-through opacity. |
| Accessibility overlay | Optional enhancement when the service is connected | User-enabled accessibility service | Can improve coverage, including some system areas | Not guaranteed to behave identically on every Android version or OEM ROM. |

On Android 12 and later, normal touch-through application overlays must respect Android's maximum obscuring-opacity policy (commonly 80%). Requested depths above that need the trusted accessibility-overlay path; otherwise Dim Veil must limit or decline the request to preserve touch-through behavior.

## Permissions and optional capabilities

See [Permissions](docs/permissions.md) for a complete table and security boundaries.

- **Display over other apps** is the normal overlay path.
- **Foreground service** keeps a user-started overlay alive while it is active.
- **Notifications** let Android show the required foreground-service notification; Android 13+ may ask for notification permission.
- **Accessibility service** is optional and renders only a dim layer. It cannot retrieve window content or perform gestures.
- **Shizuku** is optional. After the user independently installs, starts, and grants Shizuku permission to Dim Veil, the app can toggle only its own accessibility-service entry. See [Accessibility](docs/accessibility.md).

## Documentation

- [Architecture](docs/architecture.md)
- [Permissions and security boundaries](docs/permissions.md)
- [Accessibility and Shizuku](docs/accessibility.md)
- [Privacy](docs/privacy.md)
- [Platform limitations](docs/limitations.md)
- [Compatibility matrix](docs/compatibility.md)
- [Testing](docs/testing.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Release process](docs/release-process.md)
- [Architecture Decision Records](docs/decisions/README.md)

## Contributing and support

- Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.
- Report reproducible defects with the provided GitHub Issue forms.
- Read [SUPPORT.md](SUPPORT.md) for usage questions and feature requests.
- Report security-sensitive issues privately as described in [SECURITY.md](SECURITY.md), not in a public issue.
- This project follows the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

[GPL-3.0-or-later](LICENSE).
