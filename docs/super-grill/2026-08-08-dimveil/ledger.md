# Grill Ledger

Heat: well-done
Service style: single-skewer
Original order: 开发公开开源 Android 应用“暗幕”，通过触摸穿透的纯黑全屏遮罩实现低于系统最低亮度的效果。
Current station: Kitchen quarantine
Question count: 30
Current question: Skewer 1 — reproducible Android skeleton

Settled decisions:
- App: 暗幕; package `com.h166278.dimveil`; public repository `h166278/dimveil`; GPL-3.0.
- Kotlin + Jetpack Compose + Material 3; Android minSdk 26, targetSdk 35; GitHub Actions builds APK.
- Pure black 0–90% overlay; no system-brightness modification or WRITE_SETTINGS.
- Presets: 夜间 62%, 阅读 42%, 游戏 28%, 自定义 first-use 50%.
- Default is normal touch-through overlay; accessibility overlay is optional enhanced coverage.
- Right-top accessibility indicator: yellow when off, green when on; no startup popup.
- Foreground notification: close action; if notification permission is denied, overlay still works.
- Removing the app from Recents stops overlay, notification, and service. Device reboot does not restore overlay.
- All user UI is Simplified Chinese; no network, ads, analytics, or character/IP source material.
- Original Dark/Light guardian visuals and original energy-core launcher icon.

Rejected sauces:
- Directly cropped character portrait assets — rejected because public distribution has copyright risk.
- System brightness writes — rejected because it changes user settings and needs failure-prone restoration.
- Automatic accessibility prompt at app entry — rejected because it interrupts users.
- Touch-blocking overlay and floating ball — rejected because they obstruct normal app use.

Load-bearing assumptions:
- Android permits the selected overlay types with the user-granted permissions.
- A foreground service notification is acceptable whenever an overlay is running.

Decision frontier:
- ready: Skewer 1 implementation.
- blocked by: none; repository creation and public push are authorized.

Fog bank:
- OEM-specific restrictions may vary and will be documented as known limitations, not promised away.

Vocabulary on probation:
- Overlay: a pure black transparent layer; not a system brightness change.
- Enhanced coverage: use of the user-enabled accessibility overlay only for rendering the layer.

Evidence locker:
- UI visual mockup: `/var/minis/workspace/belial-dim-mockup/index.html`.
- Android build environment lacks SDK/Gradle; GitHub Actions is the settled build route.

Kitchen brigade:
- Coordinator / requirements and architecture / active

Task temperatures:
- Requirements / rested / inspected
- Architecture / rested / inspected
- Written specification / sizzling / awaiting user inspection

Deferred inspector findings:
- None.

Achievements unlocked:
- Michelin Detour — 25 decision questions reached.

Artifacts:
- `spec.md`
- `ledger.md`

Exit phrase: take it off the grill
