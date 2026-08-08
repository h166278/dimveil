# Architecture

Dim Veil separates user intent, persisted settings, actual overlay state, and Android window hosts so the UI does not report an optimistic state that Android failed to apply.

```text
MainActivity / HomeScreen
            │ user actions and lifecycle refresh
            ▼
       MainViewModel ──────────────── DataStoreDimPreferences
            │                                  │
            │ commands / presented settings     └── mode, depth
            ▼
       OverlayService ─────────────── OverlayRuntime (actual state)
            │                                      ▲
            │ chooses one host                     │ status/errors
     ┌──────┴─────────┐                            │
     ▼                ▼                            │
Normal OverlayController   AccessibilityOverlayHost │
(SYSTEM_ALERT_WINDOW)      (TYPE_ACCESSIBILITY_OVERLAY)
```

## Key components

- `MainViewModel` combines persisted settings, temporary UI preview state, Android permissions, accessibility availability, and `OverlayRuntime` into `MainUiState`.
- `DataStoreDimPreferences` persists only mode and depth.
- `OverlayService` owns foreground-service lifecycle, chooses the usable host, creates/removes the actual window, and restores user settings after a sticky-service recreation.
- `OverlayController` owns window creation and updates.
- `AccessibilityOverlayHost` attaches only while `DimAccessibilityService` is connected and tells the running service to reroute when availability changes.
- `OverlayRuntime` is the in-process source of truth for active state, requested/applied depth, active host, and failures.
- `OverlayPolicy` chooses accessibility first when it is connected, otherwise the normal overlay when permission exists.

## Invariants

1. At most one actual dim window is shown at a time.
2. UI state reflects `OverlayRuntime`, not a guessed "started" value.
3. A host connection/disconnection reroutes a currently active overlay.
4. A window or foreground-service failure is surfaced as an explicit error and the overlay is removed.
5. Normal overlays must obey the Android touch-through opacity policy; accessibility overlays are the trusted path for deeper requests.

## Lifecycle

A user start/update reaches `OverlayService`, which calls `ensureForeground()` and chooses the best available host. After a successful show it removes the other host, writes actual state to `OverlayRuntime`, and refreshes the notification. `START_STICKY` permits restoration after system process reclamation; removing the app from Recents intentionally stops it.

See [Architecture Decision Records](decisions/README.md) for the reasoning behind these choices.
