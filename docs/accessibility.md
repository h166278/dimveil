# Accessibility overlay and Shizuku

Dim Veil's optional service is named **暗幕全屏覆盖服务**. It exists only to render a black, touch-through dim layer using Android's accessibility-overlay window type.

## Strict service boundary

The service does **not** retrieve window content, inspect accessibility nodes, read text, perform gestures, click controls, control other applications, or monitor input.

Its Android configuration explicitly disables window-content retrieval and gesture performance. It only connects an overlay host while the service is enabled and connected by Android.

## Enabling the service

The default method is Android's Accessibility settings, where the user can enable or disable the service at any time.

Dim Veil also offers an optional Shizuku-assisted toggle. This works only when all of the following are true:

1. The user independently installed and started Shizuku.
2. The user explicitly granted Dim Veil permission in Shizuku.
3. The user initiates the toggle in Dim Veil.

With that permission, Dim Veil uses Shizuku's shell identity to update `Settings.Secure.enabled_accessibility_services`: it adds or removes only Dim Veil's own service component and preserves other enabled services. It does not request root, silently obtain Shizuku authorization, or change unrelated accessibility services.

If Shizuku is unavailable, not running, denied, or fails, enabling remains a manual Android Settings action.

## Why it is optional

The normal application-overlay path is sufficient for ordinary use. Accessibility overlays can improve visual coverage on some Android versions and OEM ROMs, and are the trusted path for requested dim depths above the normal touch-through overlay limit on Android 12+. They still cannot guarantee identical coverage on all devices.

For platform restrictions, see [limitations.md](limitations.md). For the design decision, see [ADR-0004](decisions/0004-accessibility-service-boundary.md) and [ADR-0005](decisions/0005-shizuku-accessibility-toggle.md).
