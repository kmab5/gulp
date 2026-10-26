# gulp — UI Review 03 (26 Oct 2026)

## Baseline and verification

Target: `kmab5/gulp` `main` at `cf91760bbe9d5c337d6886e856850cec63a93cae`. Source update is provided as an incremental overlay and Git diff. Build not executed here; Android SDK/dependency network not available. CI failure in attached log was **setup-android@v3** requesting retired SDK `tools`; replaced with v4 and `packages: ''`.

## Screen work

- Added scene-aware blur source beneath `FrostPanel` and Home shortcut tiles. The source mirrors the moving fullscreen fluid at shared screen coordinates, then uses Compose's rendering blur (API 31+). On Android 9–11 tinted/frosted fallback remains because GPU RenderEffect is unavailable. The screen itself is not snapshotted or transmitted.
- Shared scrollbar edge frost uses `ScrollState.canScrollBackward` and `canScrollForward`, so it appears only when content is clipped on that edge. Animated changes prevent abrupt overlays. Top edge is absent at the top; bottom absent when content fits or reaches its end. Unlike the old dark opacity mask, it contains a blurred fluid scene. This is a **blurred scene overlay**, not a live blur of text/cards passing beneath: that would require render-layer capture and additional compatibility testing.
- Home retains a fixed Next reminder / gulp header. Bento guides use identical heights and 10dp row spacing to the actual grid, including wide tiles.
- Navbar background cleared. The active highlight alone is frosted, animated in width-position/height; label reveal shifts icons to preserve visual centering. Icon glyphs are drawn once, white, to remove duplicated-glyph white halos. They continue a subtle motion driven by water `scene.seconds`.
- Onboarding uses Fluid Nucleus submark in first step and a small animated wave-shaped progress fill. Respects reduced motion. No impact on science/reference or notification rules.
- Shared text/water contrast now clips each of the two text colors to **opposite halves** rather than painting full white glyphs under the black overlay.
- Settings contains feedback links to the separate `kmab-feedback` public browser form (product `gulp`, source `android`, version). No in-app API secrets, Android INTERNET permission, or automatic data upload. New About gulp card shows version and repository.

## Testing on device (required before sign-off)

1. Build / test / lint on SDK 36, especially Compose package/version resolution.
2. Inspect frosted tiles as the waterline crosses them. Compare API 31+ and pre-31 appearance; test a low-end device for frame time and battery.
3. Test Nav selected backdrop only, labels at launch and after each tab switch, monochrome white icons, and tilt/ripple wobble.
4. Scroll a short screen: zero edges. Scroll a long screen at top: bottom only. Middle: both. Bottom: top only. Repeat in Home, onboarding, reminders, history, review, settings.
5. Verify that the scroll overlays visually obscure content sufficiently without becoming a dark rectangle. Confirm no tap suppression or clipped semantics.
6. Test first onboarding icon, progress, wake/bedtime, and reduced-motion behavior.
7. Open Settings > Feedback links and confirm product=gulp and version; the feedback website must register the product slug `gulp` (site admin must configure it).

## Known limitations

- `Modifier.blur` is layer/surface blur; we provide a duplicate water scene in the panel instead of falsely blurring a flat translucent tint. Scroll-edge overlay likewise blurs the water scene, not sampled text/content. This is a performance/compatibility trade-off and must be visually approved on device.
- Native Android compilation and render-performance tests remain outstanding in this environment. No claim of production readiness is made.
