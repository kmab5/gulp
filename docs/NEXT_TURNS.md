# gulp — ongoing plan after Phase 5

## Phase 5 handed off (source implementation)

Phases 1–5 remain in the full repository archive. Phase 5 adds launcher widgets, Settings appearance/accessibility preferences, personal-data export and irreversible confirmed deletion. See README and PHASE5_ARCHITECTURE.md for integration and testing.

## Phase 6 — quality and release

1. Ensure the actual Android SDK build and CI tests pass; fix any Gradle, Compose, Glance or KSP errors.
2. Run `docs/PHASE5_DEVICE_TEST.md` and earlier test checklists on real devices/launchers.
3. Profile the full-screen water engine on mid-range Android hardware and improve animations/contrast where required.
4. Validate battery usage, quiet-hour alarms, data flow, accessibility, widget/launcher lifecycle, and dark/light appearance.
5. Reduce package size, run R8 checks, prepare signed Android release candidate.

**Always deliver full updated repository ZIPs rather than patches. Widgets are part of the initial release.**
