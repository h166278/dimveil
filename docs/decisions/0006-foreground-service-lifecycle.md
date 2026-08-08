# ADR-0006: Use a foreground service with deliberate stop and recovery semantics

- Status: Accepted
- Date: 2026-08-08
- Decision makers: Dim Veil maintainers

## Context

A visible user-controlled overlay may outlive the activity. Android background execution limits require foreground-service behavior for reliable ongoing work, while users must retain a clear way to stop it.

## Decision

`OverlayService` starts in the foreground before creating an overlay and returns `START_STICKY` so it can restore persisted settings after Android reclaims its process. Removing the app from Recents deliberately stops the overlay; device reboot does not auto-restore it.

## Constraints and invariants

- Foreground startup failure is reported and does not leave a ghost state.
- The notification reflects the actual applied depth and selected mode.
- Explicit stop and Recents removal remove both possible hosts.

## Alternatives

- **Activity-only overlay:** disappears with activity lifecycle.
- **Non-foreground background service:** unreliable and conflicts with Android policy.
- **Auto-start after boot:** increases persistence beyond the product's intended user control.

## Consequences

Foreground-service restrictions and OEM power policies remain a documented compatibility concern.

## Implementation and validation

`OverlayService`, `DimNotificationFactory`, `OverlayRuntime`, and lifecycle regression tests.
