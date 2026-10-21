# Phase 2 — validation checklist

## Checks completed in packaging environment
- [x] `tools/verify_core.sh` pure Kotlin navigation/progress assertions
- [x] `tools/verify_water.sh` geometry/clamping/damping assertions
- [x] `tools/verify_structure.py` XML/Gradle/design references and expected source paths
- [x] ZIP integrity and complete Phase 1 + Phase 2 source tree

## Still required on Android SDK / real device
- [ ] `gradle --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`
- [ ] Install `app/build/outputs/apk/debug/app-debug.apk` on Android 9+ phone/emulator
- [ ] All five tabs open and the active selection slides smoothly, no nav background island
- [ ] Tilt device left/right; surface inclines smoothly and settles, no violent snapping
- [ ] Tap empty Home background, controls, scroll; ripple responds without blocking actions
- [ ] Open Water preview, move slider repeatedly, reverse direction before transition finishes
- [ ] Watch water cut horizontally through `gulp`, total text and nav icons; pixel boundary follows waterline
- [ ] Disable system animations and verify static surface/no motion; re-enable to restore
- [ ] Background app: sensors unregister, frame loop pauses, no elevated idle power draw
- [ ] Rapidly change tabs while tilting and tapping; no crashes or discontinuities
- [ ] Rotate device / change window dimensions; no crash; assess nav always submerged at 0 preview
- [ ] Test small-screen, large-font, TalkBack and landscape text overflow
- [ ] Profile 60 Hz / high-refresh devices and midrange GPU, measure p95/p99 frames
- [ ] Record APK/AAB device-specific size, startup time and idle battery before claiming targets met

The package is a **source implementation**; absent Android SDK means this checklist cannot be marked fully complete, and no precompiled APK is included.
