# 暗幕 Catering Plan

> Required cooking route: Lonely Chef With Clipboard（当前会话无独立代码子代理；每一串仍需 Raw → Sizzle → Rest 与范围检查）。

Original order: 交付 GPL-3.0 公开 Android 应用“暗幕”，以可触摸穿透的纯黑覆盖层实现低于系统最低亮度的视觉效果，并由 GitHub Actions 构建 APK。

Architecture: Compose UI + DataStore preferences + foreground normal-overlay service + optional accessibility-overlay service. `OverlayController` owns exactly one active overlay and receives a `WindowManager` from the currently active host service.

Kitchen equipment: Kotlin, Gradle, Android Gradle Plugin, Compose Material 3, DataStore Preferences, JUnit, GitHub Actions Ubuntu runner.

## Restaurant-wide constraints

- Package: `com.h166278.dimveil`; minSdk 26; targetSdk 35; GPL-3.0.
- UI is Simplified Chinese only. No network permission, ad SDK, analytics, account, cloud sync, `WRITE_SETTINGS`, screen-content reading, gesture injection, automation, floating ball, or copyrighted character assets.
- Pure black touch-through overlay has integer depth 0–90 only.
- Presets: 夜间=62, 阅读=42, 游戏=28, 自定义 initially=50 and thereafter remembers its own edited depth.
- Normal overlay is default. Accessibility overlay is optional enhancement and never automatically prompted at app entry.
- Accessibility icon is right-top only: yellow if disabled, green if enabled. Yellow opens an explanation then Android accessibility settings; green opens status/settings only.
- Starting overlay prompts for missing normal overlay permission only after the user presses the main toggle.
- If overlay runs: foreground service and, when notification permission exists, one notification with title `暗幕正在运行`, body `遮罩深度 {N}% · {模式名}`, content intent to MainActivity, and `关闭遮罩` action.
- Notification permission denial must not prevent overlay operation; in-app state must explain the missing notification close shortcut.
- Removing app from Recents must remove overlay, cancel notification, and stop service. Reboot must not restart overlay. Preferences may persist, runtime enabled state must not.
- All source and docs must use original “光能守卫 / 暗影守卫” and energy-core visuals only.

## Skewer 1: Reproducible open-source Android skeleton

Files:
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`
- Create: `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/src/main/AndroidManifest.xml`
- Create: `README.md`, `LICENSE`, `.gitignore`, `.github/workflows/android.yml`
- Test: `.github/workflows/android.yml`

Interfaces:
- Consumes: GitHub Actions `ubuntu-latest`
- Produces: `app/build/outputs/apk/debug/app-debug.apk`

- [ ] Raw: add a workflow validation step that runs `./gradlew testDebugUnitTest assembleDebug` and fails until the Gradle skeleton exists.
- [ ] Prove rawness: run the workflow after initial intentionally incomplete skeleton.
      Expected: FAIL because the requested Gradle tasks or wrapper do not exist.
- [ ] Sizzle: create a Kotlin Android/Compose project targeting minSdk 26 and targetSdk 35; configure deterministic Java 17, debug APK output, GPL-3.0, README, and artifact upload.
- [ ] Check temperature: run GitHub Actions workflow.
      Expected: PASS with `testDebugUnitTest`, `assembleDebug`, and an `dimveil-debug-apk` artifact.
- [ ] Rest: remove unused template assets/dependencies and verify no INTERNET or WRITE_SETTINGS permission exists.
- [ ] Recheck temperature: run GitHub Actions workflow and `grep -RniE 'INTERNET|WRITE_SETTINGS|firebase|analytics|ads' app README.md`.
- [ ] Inspect: compare project metadata and workflow against the recipe and constraints.

## Skewer 2: Domain model and durable local preferences

Files:
- Create: `app/src/main/java/com/h166278/dimveil/domain/DimMode.kt`
- Create: `app/src/main/java/com/h166278/dimveil/domain/DimSettings.kt`
- Create: `app/src/main/java/com/h166278/dimveil/data/DimPreferences.kt`
- Create: `app/src/main/java/com/h166278/dimveil/data/DataStoreDimPreferences.kt`
- Create: `app/src/test/java/com/h166278/dimveil/domain/DimModeTest.kt`
- Test: `app/src/test/java/com/h166278/dimveil/domain/DimModeTest.kt`

Interfaces:
- Consumes: `DimMode`, user-selected depth `Int`
- Produces: `DimSettings(mode: DimMode, depth: Int, customDepth: Int)` and `Flow<DimSettings>`

- [ ] Raw: write tests that assert exact preset values, depth clamping at 0 and 90, and that custom depth is independent.
- [ ] Prove rawness: run `./gradlew testDebugUnitTest --tests '*DimModeTest'`.
      Expected: FAIL because model and clamping behavior are absent.
- [ ] Sizzle: implement enum/defaults, immutable settings, DataStore mapping, and update operations. Do not persist a runtime enabled flag.
- [ ] Check temperature: run `./gradlew testDebugUnitTest --tests '*DimModeTest'`.
      Expected: PASS for 28/42/50/62/90 values and clamping behavior.
- [ ] Rest: centralize keys/defaults and remove duplicated mode-to-name mappings.
- [ ] Recheck temperature: run `./gradlew testDebugUnitTest`.
- [ ] Inspect: verify persisted data is limited to local mode/depth values.

## Skewer 3: Overlay primitive and deterministic state logic

Files:
- Create: `app/src/main/java/com/h166278/dimveil/overlay/OverlayState.kt`
- Create: `app/src/main/java/com/h166278/dimveil/overlay/OverlayWindowParams.kt`
- Create: `app/src/main/java/com/h166278/dimveil/overlay/OverlayController.kt`
- Create: `app/src/test/java/com/h166278/dimveil/overlay/OverlayWindowParamsTest.kt`
- Test: `app/src/test/java/com/h166278/dimveil/overlay/OverlayWindowParamsTest.kt`

Interfaces:
- Consumes: `depthPercent: Int`, `OverlayHostKind { NORMAL, ACCESSIBILITY }`, Android `WindowManager`
- Produces: `OverlayWindowParams(alpha: Float, flags: Int, type: Int)` and one black `View`

- [ ] Raw: test 0/90 alpha mapping, clamping, normal/accessibility window-type selection, and required `FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE` flag bits.
- [ ] Prove rawness: run `./gradlew testDebugUnitTest --tests '*OverlayWindowParamsTest'`.
      Expected: FAIL because parameter factory is absent.
- [ ] Sizzle: implement a single-owner controller that adds/updates/removes one pure-black view; make removal idempotent; use service-provided WindowManager.
- [ ] Check temperature: run `./gradlew testDebugUnitTest --tests '*OverlayWindowParamsTest'`.
      Expected: PASS for all depth and touch-through assertions.
- [ ] Rest: isolate Android framework calls behind a narrow adapter where needed; ensure no brightness API is referenced.
- [ ] Recheck temperature: run `./gradlew testDebugUnitTest` and `grep -RniE 'Settings.System|screenBrightness|WRITE_SETTINGS' app/src`.
- [ ] Inspect: verify controller neither reads windows nor injects touch events.

## Skewer 4: Runtime hosts, permissions, notification, and cleanup

Files:
- Create: `app/src/main/java/com/h166278/dimveil/service/OverlayService.kt`
- Create: `app/src/main/java/com/h166278/dimveil/service/DimAccessibilityService.kt`
- Create: `app/src/main/java/com/h166278/dimveil/service/OverlayCommand.kt`
- Create: `app/src/main/java/com/h166278/dimveil/service/DimNotificationFactory.kt`
- Create: `app/src/main/res/xml/dim_accessibility_service.xml`
- Create: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `app/src/test/java/com/h166278/dimveil/service/OverlayCommandTest.kt`
- Test: `app/src/test/java/com/h166278/dimveil/service/OverlayCommandTest.kt`

Interfaces:
- Consumes: `OverlayCommand.Start(depth: Int, mode: DimMode)`, `OverlayCommand.Update(depth: Int, mode: DimMode)`, `OverlayCommand.Stop`
- Produces: active overlay lifecycle, notification lifecycle, `AccessibilityStatus(enabled: Boolean)`

- [ ] Raw: write command validation tests for clamped starts/updates and stop transitions; add manifest assertions by inspection checklist for only allowed services/permissions.
- [ ] Prove rawness: run `./gradlew testDebugUnitTest --tests '*OverlayCommandTest'`.
      Expected: FAIL because command reducer and services are absent.
- [ ] Sizzle: implement normal `TYPE_APPLICATION_OVERLAY` foreground host, notification channel/action, `onTaskRemoved()` cleanup, and stop behavior. Implement an accessibility service whose configuration does not request window-content retrieval and whose only operation is an accessibility overlay; on disconnect, remove that overlay. Make notification display conditional on Android 13+ notification authorization while preserving service behavior.
- [ ] Check temperature: run `./gradlew testDebugUnitTest --tests '*OverlayCommandTest' assembleDebug`.
      Expected: PASS and an installable debug APK is produced.
- [ ] Rest: make all cleanup paths call the same idempotent remove function; use Chinese resource strings only.
- [ ] Recheck temperature: run `./gradlew testDebugUnitTest assembleDebug`; inspect manifest and `dim_accessibility_service.xml` for no content-reading/automation capability.
- [ ] Inspect: verify all three stop paths (main toggle, notification action, `onTaskRemoved`) remove the overlay and notification.

## Skewer 5: Compose home screen and permission-state behavior

Files:
- Create: `app/src/main/java/com/h166278/dimveil/MainActivity.kt`
- Create: `app/src/main/java/com/h166278/dimveil/ui/DimVeilApp.kt`
- Create: `app/src/main/java/com/h166278/dimveil/ui/MainViewModel.kt`
- Create: `app/src/main/java/com/h166278/dimveil/ui/MainUiState.kt`
- Create: `app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
- Create: `app/src/main/java/com/h166278/dimveil/ui/theme/Color.kt`
- Create: `app/src/main/java/com/h166278/dimveil/ui/theme/Theme.kt`
- Create: `app/src/test/java/com/h166278/dimveil/ui/MainViewModelTest.kt`
- Test: `app/src/test/java/com/h166278/dimveil/ui/MainViewModelTest.kt`

Interfaces:
- Consumes: `Flow<DimSettings>`, `AccessibilityStatus`, overlay command dispatcher, normal-overlay permission checker
- Produces: `MainUiState` and user-intent events `toggleOverlay`, `selectMode`, `setDepth`, `openAccessibilityInfo`

- [ ] Raw: test that selecting modes emits exact depths, 80+ warning is true only at/above 80, and a missing normal permission produces a request only on `toggleOverlay`—not on initial state refresh.
- [ ] Prove rawness: run `./gradlew testDebugUnitTest --tests '*MainViewModelTest'`.
      Expected: FAIL because view-model behavior and event rules are absent.
- [ ] Sizzle: build Chinese-only deep-space home UI from the approved mockup; use original guardian assets and energy-core icon; place accessibility state in the right top; provide yellow/green click behavior; wire permissions and service commands. Request POST_NOTIFICATIONS only for notification shortcut; if denied, show the low-interference in-app status.
- [ ] Check temperature: run `./gradlew testDebugUnitTest --tests '*MainViewModelTest' assembleDebug`.
      Expected: PASS with interaction state logic and debug APK.
- [ ] Rest: extract composables, use accessibility labels/content descriptions, and remove unused decorative code.
- [ ] Recheck temperature: run `./gradlew testDebugUnitTest assembleDebug` and render visual screenshot on an API 35 emulator in GitHub Actions if available.
- [ ] Inspect: compare the rendered result with approved dark visual direction; confirm status icon is right top, not left.

## Skewer 6: Original visual assets, documentation, and release verification

Files:
- Create: `app/src/main/res/drawable/guardian_aurora.xml`
- Create: `app/src/main/res/drawable/guardian_ember.xml`
- Create: `app/src/main/res/drawable/ic_dimveil_foreground.xml`
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- Modify: `README.md`, `.github/workflows/android.yml`, `app/src/main/res/values/strings.xml`
- Create: `docs/privacy.md`, `docs/accessibility.md`, `docs/limitations.md`
- Test: repository policy scan in `.github/workflows/android.yml`

Interfaces:
- Consumes: source tree, manifest, assets, built APK
- Produces: original asset set, policy report, debug APK GitHub Artifact

- [ ] Raw: add a policy scanner configured to fail if source/assets contain reference character names, `.jpg/.png` imported portrait assets, Internet/ads/analytics dependencies, or prohibited automation/content APIs.
- [ ] Prove rawness: run the scanner against a controlled prohibited-marker fixture before deleting it.
      Expected: FAIL and identify the marker.
- [ ] Sizzle: add original vector/adaptive assets, concise English repository docs, Chinese privacy/accessibility disclosures, repository limitations, GPL notice, and the production policy scanner.
- [ ] Check temperature: run GitHub Actions workflow.
      Expected: PASS tests, policy scan, APK build, and Artifact upload.
- [ ] Rest: validate assets at launcher and in-app dimensions; remove test fixture and all unused generated material.
- [ ] Recheck temperature: run GitHub Actions workflow and inspect artifact listing.
- [ ] Inspect: confirm public repository contains no user-supplied character image, character name, embedded telemetry, secret, or unlicensed asset.

## Plan cross-examination

### Coverage deposition

- UI, modes, slider, 80% warning, permission timing: Skewers 2 and 5.
- Pure black, depth cap, touch-through, no brightness writes: Skewer 3.
- Normal/accessibility hosts, lifecycle, notification, task removal: Skewer 4.
- Copyright-safe visuals, GPL, public documentation, no-network policy: Skewers 1 and 6.
- GitHub Actions build and APK delivery: Skewers 1 and 6.

### Contraband scan

No placeholders, speculative features, external SDKs, direct character assets, or unapproved scope additions are present.

### Interface lineup

`DimSettings`, `OverlayCommand`, `OverlayWindowParams`, `OverlayController`, `AccessibilityStatus`, and `MainUiState` are defined once and consumed in dependency order.

## Cooking-route handoff

This plan requires user approval before repository creation and implementation. The approved route is **Lonely Chef With Clipboard**: execute skewers sequentially, with fresh command evidence and a separate final branch inspection before the GitHub push is treated as complete.
