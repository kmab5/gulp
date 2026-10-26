# gulp — Fluid Nucleus brand kit

Approved identity: **Option A / Fluid Nucleus**.

## Contents

- `svg/gulp-fluid-nucleus-submark.svg`: full-color symbol with navy, blue and aqua.
- `svg/gulp-fluid-nucleus-full-logo.svg`: full-color symbol + outlined lowercase **gulp** wordmark (does not require an installed font).
- `svg/gulp-fluid-nucleus-symbolic.svg`: `currentColor` single-color cutout for CSS, dynamic tinting and themes.
- `svg/gulp-fluid-nucleus-full-logo-dark.svg`: white wordmark for dark surfaces.
- `svg/monochrome/`: navy, blue, aqua, white and black SVGs, also on their matching backgrounds.
- `png/`: sizes for previews, marketing, and mockups; SVG is the canonical source.
- `android/`: adaptive launcher foreground and monochrome Android VectorDrawable XML.
- `fluid-nucleus-brand-preview.png`: overview sheet.
- `reference/`: selected Option A concept board, for **visual comparison only** (not an app icon asset).

## Palette

| Layer | HEX |
| --- | --- |
| Navy | `#082C68` |
| Blue | `#2F6BFF` |
| Aqua | `#3EE6D9` |
| Launcher backdrop | `#E9F7FA` |

## Scaling & constraints

Symbol uses three filled closed organic Bezier contours in 256 × 256 vector coordinates. Monochrome treatment uses one organic silhouette with a knockout center (`fill-rule=evenOdd`), preserving its identity without gradients, colors, or raster effects. Keep generous clear space. Android artwork is scaled inside the adaptive icon safe region rather than stretching edge-to-edge. For dynamic coloring set CSS `color` on the symbolic SVG or tint the Android monochrome artwork.

## License / source

Custom icon geometry was created for this gulp identity, informed by the selected concept board. The wordmark outlines were generated from an installed Inter Display Bold face; this kit contains **paths only**, not font software. Inter is licensed under the SIL Open Font License (see https://github.com/rsms/inter). These files are intended for use in the gulp project; no trademark registration has been conducted.
