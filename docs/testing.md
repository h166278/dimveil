# Testing

## Automated checks

GitHub Actions runs the supported validation command on every push and pull request:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug --stacktrace
```

The workflow also runs a policy scan that rejects Internet permission, system-brightness writes, common analytics markers, automation-related markers, and raster image assets in the app source.

Current unit-test coverage includes dim-mode behavior, overlay policy, runtime state, and window parameters.

## Required manual regression checks

Before a release or a change touching overlay behavior, test on a physical device where possible:

1. Start, update, and stop at requested depths 0%, 80%, 81%, and 90%.
2. Test normal-overlay permission absent, granted, and revoked while running.
3. Test accessibility service disabled, enabled-but-not-yet-connected, connected, and disabled while running.
4. Test Shizuku unavailable, not authorized, authorized, enable, disable, and failure fallback.
5. Verify underlying taps, text input, and gestures remain usable where touch-through is expected.
6. Check status bar, notification shade, display cutout area, and Recents behavior.
7. Remove the app from Recents and verify the overlay stops.
8. Verify recovery after Android reclaims the foreground-service process when feasible.
9. Record manufacturer, ROM, Android version, and which overlay path was active.

## Test evidence

For UI or compatibility changes, attach screenshots or a short recording with private information redacted. Never include Shizuku credentials, API keys, private notifications, or unrelated app content in an issue or pull request.
