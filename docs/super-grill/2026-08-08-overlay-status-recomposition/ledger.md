# Grill Ledger

Heat: well-done
Service style: single-skewer
Original order: Recompose Dimveil overlay status UI: move accessibility warning responsibility from the top icon to the lower card, show accessibility enablement actions, express the active overlay host below the core switch, and use a normal-run icon only while an overlay is actually running.
Current station: Closing-time tribunal — branch disposition
Question count: 6
Current question: Commit and push the implementation, then trigger the GitHub Actions build?

Settled decisions:
- The lower status card owns accessibility enhancement messaging.
- When accessibility is disabled, the card states: "未开启无障碍覆盖，开启后可获得更完整的覆盖范围".
- The accessibility card is the tap target that opens the existing "无障碍覆盖" dialog; only that dialog offers "关闭" and "去开启".
- The top-right corner displays nothing at all in every state; the header icon is removed entirely.
- The top-right icon decision was reversed: no icon (neither WarningAmber nor a running icon) is shown in the header.
- While active, the label below the core switch names the current overlay host: "当前遮罩：悬浮窗遮罩" or "当前遮罩：无障碍遮罩".
- While inactive, the label is derived from available permission paths: "无障碍遮罩（待开启）" if accessibility is enabled (priority), else "悬浮窗遮罩（待开启）" if normal overlay is granted, else "需要悬浮窗或无障碍权限".
- The floating-window permission entry remains reachable: tapping the core switch while blocked falls through to MainActivity's existing fallback (accessibility settings if the service is enabled, otherwise overlay permission settings).
- The disabled-accessibility dialog text will be trimmed to the exact user copy "未开启无障碍覆盖，开启后可获得更完整的覆盖范围".

Rejected sauces:
- Shield for authorized-but-idle state — rejected because the user wants no header icon at all.
- Any header icon in any state — rejected by the user's final call "右上角就啥也不显示" (Return to Marinade: supersedes the earlier exclamation-then-running-icon plan).

Load-bearing assumptions:
- "关闭" on the accessibility card dismisses the card/dialog only; it does not disable an already-enabled Android accessibility service.
- The core switch remains physically clickable even when visually disabled (its clickable modifier has no enabled flag), so the blocked-tap permission fallback works.
- The working tree's uncommitted HomeScreen/MainActivity changes are the user's intended baseline for this feature.

Decision frontier:
- ready: commit & push & trigger CI build (needs user authorization).
- blocked by: user confirmation.

Achievements unlocked:
- Question 5 (spec approval): Amuse-Bouche Complete.
- All three skewers passed first-pass verification, no fix rounds: Certified Nothingburger candidate.

Fog bank:
- None.

Vocabulary on probation:
- "悬浮窗遮罩": ordinary TYPE_APPLICATION_OVERLAY host.
- "无障碍遮罩": accessibility-service overlay host.
- "实际运行": OverlayRuntime reports active with a non-null host.

Evidence locker:
- HomeScreen.kt final state verified: no header icon, five-state label, status card with hint chain, exact dialog copy (grep 16/16 PASS).
- MainActivity.kt: onOpenOverlayPermission parameter removed; openOverlayPermission() definition (L61) and onToggle fallback reference (L42) intact.
- CoreSwitch clickable has no enabled gate — blocked-tap permission fallback confirmed.

Kitchen brigade:
- Pantry Scout / repository evidence / complete / HomeScreen.kt and MainUiState.kt read.
- Lonely Chef (inline) / implementation / complete / Skewers 1-3 all PLATED.

Task temperatures:
- Skewer 1 (dialog copy) / raw / sizzling / rested / inspected: done — exact string at L150, old string 0 occurrences.
- Skewer 2 (runtime hint chain) / raw / sizzling / rested / inspected: done — 4 hint terms 1 occurrence each, fixed copy isolated at L426, OverlayError import restored (matches HEAD, hence no diff hunk).
- Skewer 3 (cleanup + full acceptance) / raw / sizzling / rested / inspected: done — 16/16 assertions PASS, StatusWarnBg removed, full diff reviewed hunk by hunk.

Deferred inspector findings:
- Pre-existing unused imports (Shield, FontFamily, offset) — present in HEAD 1a76c69, not introduced here; Kotlin warning only, parked per walk-in freezer.
- No local compilation (sandbox lacks Android SDK); Kotlin syntax verified by diff review, compile verification delegated to CI.


Artifacts:
- docs/super-grill/2026-08-08-overlay-status-recomposition/ledger.md

Exit phrase: take it off the grill
