# Troubleshooting

## The overlay will not start

- Grant **Display over other apps** permission, or enable the optional accessibility service.
- On Android 13+, allow notifications if Android blocks foreground-service notification behavior.
- Reopen Dim Veil after returning from a system Settings screen so permission state refreshes.

## A deep dim level does not apply

On Android 12+, a normal touch-through overlay is constrained by the system's maximum obscuring opacity, commonly 80%. Enable **暗幕全屏覆盖服务** if you need a deeper requested level and understand the accessibility-service boundary. See [limitations.md](limitations.md).

## Status bar or notification shade is not dimmed

This is expected on some systems with the normal overlay path. The optional accessibility-overlay path may improve coverage, but OEM policy can still prevent uniform results.

## The accessibility service did not toggle

Shizuku-assisted toggling requires Shizuku to be running and Dim Veil to be explicitly authorized in the Shizuku app. Otherwise use Android Accessibility settings to enable or disable **暗幕全屏覆盖服务** manually.

## The overlay disappeared

Removing Dim Veil from Recents intentionally stops it. Android may also reject or stop foreground work under device-specific power policies. Check the app's status message and grant the required permission again if it was revoked.

## Reporting a bug

Use the Bug Report form and include your device/ROM version, active overlay path, requested depth, steps to reproduce, and whether touch-through worked. Redact personal information from screenshots and logs.
