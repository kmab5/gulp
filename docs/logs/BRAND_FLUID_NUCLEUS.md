# gulp — Fluid Nucleus (approved identity)

The selected identity is **Fluid Nucleus / Option A**. Three nested, independently drawn organic vector shapes create the full-color submark. The mono submark uses the exact outer contour with the inner nucleus as a transparent knockout (not a painted white shape).

## Palette

- Navy `#082C68` — outer shape and full-logo wordmark
- Blue `#2F6BFF` — middle layer
- Aqua `#3EE6D9` — nucleus
- Pale launcher background `#E9F7FA`

Source files: `design/brand/`, and the deliverable archive `gulp_fluid_nucleus_brand_kit.zip` (separately provided). Android adaptive icon XML: `app/src/main/res/drawable/ic_gulp_foreground.xml` and `ic_gulp_monochrome.xml`. Do not use rasterized board art as the icon.

## Home header behavior

Top-right **gulp** wordmark is a 92 × 48 dp tap target. Tap toggles between "gulp" and the **monochrome submark**, using a short interrupted-friendly crossfade/scale transition. Both render with `WaterAdaptiveText` / `WaterAdaptiveSubmark`, which share `waterBelowClip` with the water scene. The waterline paints the submerged pixels in dark and unsubmerged pixels in light; no whole-label flip. State is held above the tab screen with `rememberSaveable` so navigation does not unexpectedly reset the selection. The top-right tap also causes a small water ripple.

## Android launcher

Adaptive foreground remains inside the central safe area. Monochrome icon is a one-color knockout for themed icons. New launcher background is deliberately pale to preserve contrast with the dark outer contour.

## Accessibility

The switch target is at least 48dp tall and has a semantic button role and state description. Decorative under-water paint copies are hidden from accessibility. No extra sign-in, permission, network, or animation engine dependencies.
