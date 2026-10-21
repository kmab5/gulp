# After Phase 6 — device-verified corrections only

The coding phases are consolidated in this repository. Phase 6 is a **source release candidate**, not a device-certified release. Next steps are to run the first Android SDK build, fix any reported errors, and visually review on a real phone. See `PHASE6_BUILD_AND_DEVICE_ACCEPTANCE.md`.

Do not claim a signed app, confirmed 60 FPS, verified notification reliability or a Play Store release until actually measured/tested. Deliver small complete update bundles against the current GitHub head, including updated documentation and explicit deletion commands when necessary.

The initial release includes two Android launcher widgets; verify both on the device before distribution.

The current visual work is tracked in `UI_FEEDBACK_AUDIT.md`. Onboarding, Home, and navbar have priority over visual changes to other tabs.
