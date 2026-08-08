# Permissions and security boundaries

Dim Veil follows a minimum-permission design. It has no network permission and keeps all preferences on-device.

| Capability / permission | Required? | Why Dim Veil uses it | Explicit boundary |
|---|---:|---|---|
| `SYSTEM_ALERT_WINDOW` | Required for normal path | Displays the normal pure-black touch-through overlay | Does not read screen pixels, intercept input, or alter other apps. |
| `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_SPECIAL_USE` | Required while an overlay runs | Keeps the user-started overlay alive and shows its foreground notification | Does not collect data or run unrelated background work. |
| `POST_NOTIFICATIONS` | Android 13+ optional/recommended | Allows the foreground-service notification to be shown normally | Never used for marketing, tracking, or push messages. |
| Accessibility service binding | Optional enhancement | Creates an accessibility-overlay window for the dim layer | `canRetrieveWindowContent=false`; no node inspection, gestures, clicks, or input monitoring. |
| Shizuku authorization | Optional convenience | Toggles Dim Veil's own accessibility-service entry after explicit user approval | Does not request root, install software, read other apps' data, or alter other accessibility-service entries. |

## Stored data

Dim Veil saves only the selected dim mode and depth using Android DataStore in its private app storage. It does not maintain an account, cloud copy, analytics identifier, or usage history.

## Shizuku scope

Shizuku is not required to use Dim Veil. If it is available and the user explicitly authorizes Dim Veil in the Shizuku app, Dim Veil reads and updates Android's `enabled_accessibility_services` secure setting solely to add or remove:

```text
com.h166278.dimveil/com.h166278.dimveil.service.DimAccessibilityService
```

When enabling, existing enabled accessibility-service entries are retained. When disabling, only the Dim Veil entry is removed. If Shizuku is unavailable or authorization is denied, the user can use Android Accessibility settings instead.

## Review triggers

A change requires explicit security review when it affects the manifest, permissions, accessibility-service configuration, Shizuku commands, window type/flags/alpha policy, foreground-service lifecycle, locally stored fields, or external dependencies.
