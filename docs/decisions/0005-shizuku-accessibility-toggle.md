# ADR-0005: Make Shizuku an explicit, scoped accessibility-toggle convenience

- Status: Accepted
- Date: 2026-08-08
- Decision makers: Dim Veil maintainers

## Context

Android normally requires users to enable an accessibility service in Settings. A user who already operates Shizuku may want a direct toggle, but changing secure settings is sensitive and must not affect unrelated services.

## Decision

Offer a Shizuku-assisted toggle only after Shizuku is running and the user explicitly grants Dim Veil permission. Read `enabled_accessibility_services`, add or remove only Dim Veil's component, preserve other entries, and update `accessibility_enabled` only when appropriate.

## Constraints and invariants

- No root request and no silent Shizuku authorization.
- Never remove, add, or reorder another service's entry intentionally.
- On unavailable/denied/failed Shizuku, fall back to Android Settings.
- Do not persist or transmit Shizuku authorization material.

## Alternatives

- **Manual Settings only:** simplest and remains the fallback, but less convenient for opted-in Shizuku users.
- **Disable every service before enabling ours:** unsafe and unacceptable.
- **Use undocumented broad system control:** exceeds the narrow requirement.

## Consequences

The implementation must preserve list contents and document the exact scope. Shizuku API changes require compatibility testing.

## Implementation and validation

`ShizukuAccessibility`, `MainViewModel.toggleAccessibility`, Shizuku permission callbacks, and manual tests with other accessibility services already enabled.
