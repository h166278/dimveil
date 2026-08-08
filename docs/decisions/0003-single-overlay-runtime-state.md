# ADR-0003: Keep one actual runtime state and one shown overlay

- Status: Accepted
- Date: 2026-08-08
- Decision makers: Dim Veil maintainers

## Context

Activity-local booleans can claim that an overlay started even when permission, foreground-service startup, or window creation failed. Two possible hosts also risk duplicate windows and inconsistent UI.

## Decision

`OverlayRuntime` is the in-process source of truth for active state, requested/applied depth, active host, and errors. `OverlayService` owns host selection and removes the non-selected host after a successful show.

## Constraints and invariants

- Only one physical overlay is visible at any time.
- Failures are explicit runtime errors, not optimistic success states.
- Accessibility attach/detach requests rerouting of an active overlay.

## Alternatives

- **UI-owned active flag:** simple but races with Android lifecycle and window failures.
- **Independent hosts without a coordinator:** risks duplicate layers and stale state.

## Consequences

Service and host code must update runtime state consistently; ViewModel combines it with persisted settings and permissions.

## Implementation and validation

`OverlayRuntime`, `OverlayService`, `AccessibilityOverlayHost`, `MainViewModel`, and `OverlayRuntimeTest`.
