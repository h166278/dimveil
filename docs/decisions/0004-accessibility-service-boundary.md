# ADR-0004: Keep accessibility usage render-only

- Status: Accepted
- Date: 2026-08-08
- Decision makers: Dim Veil maintainers

## Context

Accessibility services are powerful and sensitive. Dim Veil needs only the accessibility-overlay window type for optional visual coverage, not accessibility automation.

## Decision

The accessibility service performs no accessibility event processing and exists only to attach an overlay controller. Its configuration disables window-content retrieval and gesture performance.

## Constraints and invariants

- No accessibility-node or window-content inspection.
- No gestures, clicks, automated actions, input monitoring, or other-app control.
- The user can disable the service in Android Settings at any time.

## Alternatives

- **Use accessibility APIs for automation:** unnecessary and outside the app's privacy model.
- **Avoid accessibility entirely:** loses a trusted overlay path and optional coverage improvement.

## Consequences

Any request that expands the service beyond rendering needs a new public security review and ADR.

## Implementation and validation

`DimAccessibilityService`, `dim_accessibility_service.xml`, manifest service declaration, and manual permission-boundary testing.
