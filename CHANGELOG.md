## 0.6.4-rc5 — UI feedback audit and follow-up (2026-10-21)

- Applied waterline-adaptive ink to exposed onboarding headings and step label (the frosted card text remains fixed high-contrast).
- Made the medical fluid plan card centered and prominent; highlighted chosen radio items amber, distinct from teal switches.
- Fixed onboarding continuation incorrectly accepting an invalid custom mL entry.
- Added rounded translucent editing slot guides behind tiles and reduced drag-hover oscillation by requiring center overlap and minimum swap interval.
- Increased navbar label size to reduce tiny glyph rasterization artifacts; real-device pixel clarity still needs review.
- Updated release version, documentation and CI contract tests against the current `com.kmab.gulp` paths.
- Deliberate visual alternatives and device-unverified behaviors are listed in `docs/UI_FEEDBACK_AUDIT.md`.

## 0.6.3-rc4 — package identity, JVM compile fix, signing

- Fixed the WaterScene property-setter collision by renaming its method to `updateReducedMotion` and updating call sites.
- Migrated Kotlin and test packages, source directories, application ID, and Gradle namespace to `com.kmab.gulp`.
- Added deterministic local release signing with ignored root `build-key` and `keystore.properties`. Release packaging cannot silently be unsigned.
- Expanded `.gitignore` using GitHub's Android/Kotlin patterns and protected signing material.
- Kept CI debug-only until securely configured release signing is intentionally enabled.

## 0.6.2-rc3 — Kotlin/Compose compilation corrections

- Fixed waterline contrast's nested drawing scope: explicitly call `this@drawWithContent.drawContent()` in the `clipPath` block.
- Added `@OptIn(ExperimentalMaterial3Api::class)` to `DrinkEditorSheet` and `TileEditorSheet`, preserving bottom-sheet behavior.
- Migrated Kotlin compiler target configuration to the `kotlin.compilerOptions` DSL (JVM 17) to eliminate Gradle's deprecation warning.
- Added `tools/verify_compose_buildfix.py` preflight and a CI step; previous source and references are untouched.
- **Unverified until run in an Android SDK environment:** complete Gradle build, runtime behavior, and lint.

## 0.6.1-rc2 — Gradle/Kotlin first-build fixes

- Fixed invalid uppercase Gradle version alias `KSP` (now `ksp`), and matching `version.ref`.
- Fixed Reminders callback referencing out-of-scope `medical` local variable.
- Added `tools/verify_gradle_catalog.py` and CI preflight to catch malformed aliases before a costly Android build.
- Native Android compilation remains to be confirmed by the user's Gradle run.

# gulp changelog

## 0.6.0-rc1 · 2026-10-21 — Phase 6 source hardening
- Local-time aware DST reminder scheduling and additional JVM regressions.
- Notification disable/clear safety, one-off customization navigation and true Home next reminder.
- Clinician-managed fluid handling, safe date editor selection, widget host access.
- Android CI expanded to debug/release build, lint, unit tests and artifact reports.
- Detailed build and device acceptance checklist. **Native build not run in this environment.**

# Change log

## 0.5.0-phase5 · 2026-10-20
- Two native Glance launcher widgets, direct actions using Room and refresh on record/date changes.
- Settings accessibility preferences persisted with DataStore; improved reduced-motion nav.
- Secure system-document CSV export and explicitly confirmed delete-all action.
- Repository CSV/delete tests, widget manifest checks, Phase 5 docs and CI artifact naming.
- Full Android build and device QA still pending; no APK claimed.

# gulp changelog

## 0.4.0-phase4 · 2026-10-20
- Added first-run onboarding, opt-in reminders and local awake/quiet schedule.
- Added Room-linked Review charts and statistics of actual saved records.
- Added optional logged-beverage tracking reference and clinician-managed flag; visual fill scale remains independent.
- Added permission-aware Android inexact one-shot alarms, explicit notification actions, reboot/timezone rescheduling, snooze and recent-drink suppression.
- Added DataStore-backed single-use notification action tokens to avoid duplicate log actions on redelivery/double taps.
- Added domain and integration-contract tests, device test checklist, science guardrail documentation.
- All previous Home, History and water-engine sources retained.

## 0.3.0-phase3
- Room drink tracking, undo, date history and DataStore Home layout.

## 0.2.0-phase2
- Procedural animated water, tilt/touch reactions, waterline text contrast.

## 0.1.0-phase1
- Kotlin/Compose project foundation and five-tab shell.
