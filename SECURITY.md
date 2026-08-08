# Security policy

## Supported versions

Security fixes are applied to the latest development branch and the latest published release when one exists.

## Report a vulnerability privately

Do **not** open a public issue for a suspected vulnerability. Use GitHub's private vulnerability reporting feature for this repository when available. If it is unavailable, contact the maintainer privately through the repository profile and include a concise report.

Include affected version/commit, device and Android version, reproduction steps, impact, and any proof of concept needed to validate the issue. Do not include unrelated personal data, Shizuku authorization material, or secrets.

Maintainers aim to acknowledge a report within 7 days, provide a status update within 30 days, and coordinate disclosure after a fix is available. These are targets, not guarantees.

## In scope

- Unauthorized permission or secure-settings changes
- Accessibility service escaping its render-only boundary
- Shizuku actions changing unrelated services or system state
- Overlay behavior that blocks input unexpectedly or enables deceptive UI behavior
- Local-data disclosure, APK signing, build, dependency, or release supply-chain issues

## Out of scope

Expected Android/OEM limitations documented in [docs/limitations.md](docs/limitations.md), unsupported modified OS behavior, and issues requiring the user to intentionally grant broad unrelated device privileges are usually out of scope unless Dim Veil amplifies their impact.

## Security design references

See [docs/permissions.md](docs/permissions.md) and [docs/decisions](docs/decisions/README.md).
