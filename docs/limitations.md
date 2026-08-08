# Platform limitations

Android and OEM window policies differ. Dim Veil provides a visual dim layer, not a system brightness control, and it cannot promise identical behavior across all devices, Android versions, launchers, lock screens, or system surfaces.

## Touch-through opacity on Android 12+

Android 12 and later apply a safety policy to touch-through application overlays. The policy evaluates the window-level `LayoutParams.alpha`, not merely a view's alpha. Normal application overlays must stay at or below Android's maximum obscuring opacity to preserve touch-through behavior; the commonly observed limit is 80%, but Dim Veil queries the platform value where available.

Requested depths above that limit require the trusted accessibility-overlay path. If it is unavailable, Dim Veil must not claim a touch-through normal overlay at that depth.

## Coverage differences

A normal application overlay may not cover status bars, notification surfaces, lock screens, or other protected system UI. An accessibility overlay can improve coverage after the user enables the optional service, but OEM policies can still differ.

## Lifecycle behavior

- Removing Dim Veil from Recents intentionally stops the overlay.
- A device reboot does not restore the overlay automatically.
- Android may delay or reject foreground-service startup under system policy; Dim Veil reports such failures instead of pretending the overlay is active.
- If Android revokes an overlay or accessibility permission, the running state can change and the user may need to grant it again.

See [testing.md](testing.md) for the regression matrix and [compatibility.md](compatibility.md) for verified devices.
