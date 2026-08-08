# ADR-0002: Enforce Android touch-through opacity policy

- Status: Accepted
- Date: 2026-08-08
- Decision makers: Dim Veil maintainers

## Context

On Android 12+, a non-touchable application overlay cannot simply be arbitrarily opaque and still be assumed to pass touches through. Android applies a maximum-obscuring-opacity safety policy.

## Decision

Normal application overlays use the platform-safe maximum depth. The policy is applied at the window level through `LayoutParams.alpha`; changing only a view alpha is insufficient. Requested depths above the normal limit require the trusted accessibility-overlay host, otherwise the request is limited or unavailable rather than falsely reported as touch-through.

## Constraints and invariants

- Preserve input safety and do not bypass Android's policy.
- Keep requested depth separate from applied depth.
- Query the platform maximum where available; do not hard-code a universal guarantee.

## Alternatives

- **Always use 90% normal overlay:** can break touch-through or be rejected by Android.
- **Use view alpha only:** does not address window-level enforcement.
- **Remove high depths:** avoids complexity but removes a core capability when the trusted path exists.

## Consequences

The UI and notification must report actual applied depth. Tests must cover the normal limit and deeper accessibility path.

## Implementation and validation

`OverlayController.normalMaxDepth`, window parameters, `OverlayRuntime`, and `OverlayWindowParamsTest`.
