# Compatibility

Android and OEM policies can change overlay coverage, permission flows, and background execution. This matrix records evidence; it is not a promise that untested devices work the same way.

| Device / ROM | Android | Normal overlay | Accessibility overlay | Shizuku toggle | Status | Evidence |
|---|---:|---|---|---|---|---|
| Xiaomi / HyperOS | Not version-pinned | Tested during development | Tested during development | Tested during development | Partial: exact device/version should be recorded before release | Maintainer testing |
| Google Pixel / AOSP | — | Unverified | Unverified | Unverified | Community testing wanted | — |
| Samsung / One UI | — | Unverified | Unverified | Unverified | Community testing wanted | — |
| OPPO / ColorOS | — | Unverified | Unverified | Unverified | Community testing wanted | — |

## Reporting a result

Use the compatibility issue form and include the device model, Android/ROM version, requested depth, active overlay path, Shizuku state, affected system area, and whether touch-through still worked. A report is added here only after it contains enough reproducible detail.
