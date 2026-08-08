# Grill Ledger

Heat: well-done
Service style: single-skewer
Original order: Refine Dim Veil's UI to match the supplied reference's visual direction.
Current station: Skewer bureaucracy
Question count: 4
Current question: Written implementation plan approval

Settled decisions:
- Use the reference as a visual direction, not a pixel-for-pixel replica.
- Choose option A: refine the existing one-screen interface without new features or screens.
- Keep original guardian naming and all existing overlay behavior.
- Keep the visual system near-black, restrained mint, and amber warning semantics.

Rejected sauces:
- Dashboard restructure -- rejected because it expands interaction scope.
- Pixel-perfect copy -- rejected because it harms device adaptation and existing state clarity.

Load-bearing assumptions:
- The application remains a single-screen Compose UI.
- GitHub Actions is the APK build and unit-test route.

Decision frontier:
- ready: approve written implementation plan
- blocked by: plan approval before workspace quarantine and implementation

Fog bank:
- Exact device screenshot dimensions depend on the CI/emulator route and are not part of this change.

Vocabulary on probation:
- Core switch: the central circular control that starts or stops the overlay.
- Visual direction: the reference's hierarchy and mood, without copying its pixels or IP.

Evidence locker:
- Reference image: /var/minis/attachments/uploads/1000042442.jpg
- Current UI: app/src/main/java/com/h166278/dimveil/ui/HomeScreen.kt

Kitchen brigade:
- Pantry Scout / repository and reference inspection / complete / conversation evidence

Task temperatures:
- UI refinement / raw

Artifacts:
- docs/super-grill/2026-08-08-ui-refinement/ledger.md
- docs/super-grill/2026-08-08-ui-refinement/spec.md
- docs/super-grill/2026-08-08-ui-refinement/plan.md

Exit phrase: take it off the grill
