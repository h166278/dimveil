# Dim Veil UI Refinement Catering Plan

> Required cooking route: Lonely Chef With Clipboard.

Original order: Refine Dim Veil's existing one-screen UI to follow the supplied reference's calm dark visual direction without copying it or changing behavior.

Architecture: `DimVeilTheme` owns semantic color and typography roles. `HomeScreen` owns scrollable composition, visual state, callbacks, and dialogs. `MainActivity`, persistence, and overlay services are unchanged.

Kitchen equipment: Git repository checks locally; GitHub Actions runs `gradle testDebugUnitTest assembleDebug --stacktrace` and publishes the debug APK.

## Restaurant-wide constraints

- Keep `光能守卫` while inactive and `暗影守卫` while active.
- Do not add screens, permissions, dependencies, images, networking, data collection, or service changes.
- Keep the current callbacks and behavior for overlay toggle, mode selection, depth persistence/update, and accessibility settings.
- Use a vertically scrollable root and stable dimensions for the mode control and core switch.
- Use near-black background, restrained mint action color, and amber warning semantics.
- Use 8dp panel corners. Do not create nested cards or decorative background effects.
- APK build and unit-test verification runs through GitHub Actions.

## Skewer 1: Establish visual tokens and scroll-safe screen composition

Files:
- Modify: `app/src/main/java/com/h166278/dimveil/ui/DimVeilTheme.kt`
- Modify: `app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
- Test: source-level structural review plus GitHub Actions build

Interfaces:
- Consumes: `HomeScreen(active: Boolean, mode: DimMode, depth: Int, accessibilityEnabled: Boolean, canDraw: Boolean, onToggle: () -> Unit, onMode: (DimMode) -> Unit, onDepth: (Int) -> Unit, onAccessibilityRefresh: () -> Unit) -> Unit`
- Produces: same public `HomeScreen` callable signature and a scrollable rendered control surface

- [ ] Raw: inspect `HomeScreen.kt` and establish the falsifiable expectation that the root lacks a `verticalScroll` modifier, so short screens can make lower controls unreachable.
- [ ] Prove rawness: run `grep -n "verticalScroll" app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
      Expected: no match before the change.
- [ ] Sizzle: introduce centralized semantic UI tokens and apply a `rememberScrollState` plus `verticalScroll` root while retaining every existing callback, title, mode, depth, and dialog behavior.
- [ ] Check temperature: run `grep -nE "verticalScroll|rememberScrollState" app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
      Expected: both scroll primitives are present.
- [ ] Rest: remove unused imports, group related modifiers and tokens, and retain readable composable boundaries.
- [ ] Recheck temperature: run `git diff --check` and inspect the public `HomeScreen` signature with `grep -n "fun HomeScreen" app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
      Expected: no whitespace error and unchanged callback surface.
- [ ] Inspect: compare the diff with the approved specification, especially no changed service or persistence files.
- [ ] Commit, only if authorized: `git add app/src/main/java/com/h166278/dimveil/ui docs/super-grill/2026-08-08-ui-refinement && git commit -m "feat: refine Dim Veil home UI"`

## Skewer 2: Refine core controls and semantic status panels

Files:
- Modify: `app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
- Test: source-level structural review plus GitHub Actions build

Interfaces:
- Consumes: `active(Boolean)`, `mode(DimMode)`, `depth(Int)`, `accessibilityEnabled(Boolean)`, `canDraw(Boolean)`
- Produces: text-supported active/inactive state, stable mode segments, 8dp un-nested panels, and depth warning at `depth >= 80`

- [ ] Raw: inspect current control constants and establish the falsifiable expectation that panels use 12dp and 14dp corners instead of the approved 8dp panel radius.
- [ ] Prove rawness: run `grep -nE "RoundedCornerShape\((12|14)\.dp\)" app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
      Expected: matches before the change.
- [ ] Sizzle: refine the central core switch, four equal-width mode segments, depth panel, and status panel to the approved hierarchy while preserving `onToggle`, `onMode`, `onDepth`, and accessibility dialog routes.
- [ ] Check temperature: run `grep -nE "RoundedCornerShape\(8\.dp\)|depth >= 80|Modifier\.weight\(1f\)" app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt`
      Expected: 8dp panel radius, high-depth guard, and equal-width mode segments are present.
- [ ] Rest: verify icon content descriptions and text labels distinguish state without relying on color, then remove duplicate color literals that have a semantic token.
- [ ] Recheck temperature: run `git diff --check` and `git diff -- app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt app/src/main/java/com/h166278/dimveil/ui/DimVeilTheme.kt`
      Expected: whitespace clean; diff limited to approved UI files.
- [ ] Inspect: confirm no external images, new permissions, new dependencies, or behavior changes were introduced.
- [ ] Commit, only if authorized: included in Skewer 1 commit when tasks are completed as one UI-only change.

## Skewer 3: Build and release evidence

Files:
- Modify: none unless CI exposes a UI-source compilation issue
- Test: GitHub Actions workflow `.github/workflows/android.yml`

Interfaces:
- Consumes: completed UI source changes
- Produces: passing JVM unit-test run and `dimveil-debug-apk` artifact

- [ ] Raw: establish baseline CI status for the target commit using the existing workflow.
- [ ] Prove rawness: record the GitHub Actions run URL and state before any rerun or push.
      Expected: baseline is either completed or explicitly unavailable locally; no fabricated visual-test claim.
- [ ] Sizzle: push the authorized commit to GitHub, which triggers the existing workflow.
- [ ] Check temperature: observe `Unit tests and debug APK` succeed in the GitHub Actions run.
      Expected: the job exits successfully and `dimveil-debug-apk` is uploaded.
- [ ] Rest: download or install the APK only if separately authorized, then visually inspect scrolling, mode selection, toggle state, high-depth warning, and permission messaging.
- [ ] Recheck temperature: rerun the workflow only if source changes occur after the successful run.
- [ ] Inspect: compare the run's tested commit SHA with the final source revision.
- [ ] Commit, only if authorized: no additional commit.

## Coverage Deposition

- Scroll-safe small-screen layout: Skewer 1.
- Visual hierarchy, core switch, mode controls, depth and status panels: Skewer 2.
- Preserved behavior and buildable APK: Skewers 1-3.
- No new authority, dependencies, or network surface: Skewers 1-2 inspection.

## Contraband Scan

The plan adds no placeholder task, external tracker, new capability, or visual asset. Local Android rendering is explicitly excluded because the sandbox lacks a Gradle/Android toolchain.

## Interface Lineup

`HomeScreen` keeps its existing parameters and callback signatures. `MainActivity`, `OverlayService`, `DataStoreDimPreferences`, and `DimMode` remain outside the edit scope.
