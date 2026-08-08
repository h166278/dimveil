# ADR-0001: Use normal and accessibility overlay hosts

- Status: Accepted
- Date: 2026-08-08
- Decision makers: Dim Veil maintainers

## Context

A visual dim layer should work with the least sensitive permission possible, but normal application overlays may not cover all system areas and are constrained by Android touch-through safety policy.

## Decision

Use a normal `SYSTEM_ALERT_WINDOW` host as the default path. Use an optional accessibility-overlay host only while the user-enabled accessibility service is connected. Prefer the accessibility host when available; otherwise use the normal host.

## Constraints and invariants

- At most one actual overlay window may be visible.
- The accessibility service must remain render-only.
- The UI must expose the actual selected host, not merely the user's preference.

## Alternatives

- **Normal overlay only:** lower permission footprint but incomplete coverage and no trusted deeper path.
- **Accessibility only:** stronger coverage on some systems but imposes a sensitive permission on all users.
- **Change system brightness:** violates the product definition and changes device-wide state.

## Consequences

Host changes are runtime events that must reroute an active overlay. Documentation and tests must distinguish both paths.

## Implementation and validation

`OverlayService`, `OverlayPolicy`, `OverlayController`, `AccessibilityOverlayHost`, and `DimAccessibilityService`; unit tests for policy and window parameters plus physical-device regression testing.
