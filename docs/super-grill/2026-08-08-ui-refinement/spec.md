# Dim Veil UI Refinement Specification

## Goal

Refine the existing Dim Veil home screen so it communicates a calm, premium dark control surface aligned with the supplied reference image while retaining every existing overlay, mode, depth, and accessibility behavior.

## Scope

- Modify only the Compose presentation layer in `ui/DimVeilTheme.kt` and `ui/HomeScreen.kt`.
- Make the home content vertically scrollable.
- Improve spacing, type hierarchy, stable control dimensions, selected states, status presentation, and the central core switch.
- Keep the current original names: `光能守卫` while inactive and `暗影守卫` while active.
- Preserve the existing near-black base, mint action color, and amber warning semantics.

## Non-goals

- No new screen, dashboard, preference, permission, service, dependency, network capability, or external bitmap asset.
- No pixel-for-pixel copy of the reference.
- No change to overlay alpha calculation, persistence schema, mode-depth rules, or accessibility behavior.

## Design

The root uses a scrollable near-black content surface with fixed horizontal padding. The top row presents product identity, a concise purpose, and the existing clickable accessibility status icon.

The central region presents the current guardian title, concise state description, the core switch, and an unambiguous overlay state label. The core switch remains the only large visual focal point. Its inactive and active states differ through semantic mint brightness and inner fill, not decorative background effects.

All four modes use an equal-width segmented row with a stable height. Selection uses mint outline and restrained fill; the inactive state remains legible with an outline only.

The depth control and coverage status appear as two independent, un-nested panels. Each panel uses an 8dp radius, clear type hierarchy, and semantic colors. A depth at or above 80 percent displays the existing amber visibility warning.

## Behavior and Failure Handling

- The core switch starts/stops the overlay exactly as before. Missing draw-over permission opens the existing system permission flow.
- Selecting a mode preserves the existing mode and depth persistence behavior.
- Moving the slider preserves live overlay updates when active.
- The accessibility status icon preserves its dialog and system-settings route.
- Permission state must accurately distinguish an enabled accessibility cover, an available regular overlay, and a pending draw-over permission.
- Vertical scrolling prevents lower controls from being unreachable on small screens or with enlarged text.

## Accessibility

- Every tappable control has a meaningful content description where an icon alone is used.
- Visual state is supported by text labels and does not depend on color alone.
- Text contrast follows the existing dark theme's on-surface and surface-variant roles.

## Verification

- Run existing JVM unit tests through GitHub Actions.
- Build a debug APK through GitHub Actions.
- Review Compose source for stable dimensions, scroll support, retained callbacks, and no new permissions or network dependencies.
- Review the rendered Android screen manually from the produced APK before release; this is required because local Android rendering is unavailable in the sandbox.

## Rollback

Reverting the UI-only commit restores the former Compose presentation without changing stored settings, services, or user permissions.
