#!/usr/bin/env python3
"""Cross-file visual review preflight; does NOT replace a native Android build."""
from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT/'app/src/main/java/com/kmab/gulp'
def has(path, *needles):
    p=ROOT/path
    assert p.is_file(), f'Missing: {path}'
    t=p.read_text()
    for needle in needles:
        assert needle in t, f'{path}: missing {needle}'

has('app/src/main/java/com/kmab/gulp/feature/onboarding/OnboardingScreen.kt',
    'fun OnboardingScreen(scene: WaterScene, viewport: IntSize',
    'WaterAdaptiveText(title, scene, viewport', 'GulpMedicalPlanCard(',
    'enabled = canContinue', 'Custom amount', 'Theme_Material_Dialog_Alert')
has('app/src/main/java/com/kmab/gulp/MainActivity.kt',
    'OnboardingScreen(scene, viewport, onComplete',
    'Box(Modifier.fillMaxSize().onSizeChanged { viewport = it })')
has('app/src/main/java/com/kmab/gulp/core/design/GulpComponents.kt',
    'fun GulpMedicalPlanCard(', 'Color(0xFFFFD38C)',
    'enabled: Boolean = true, content:', '.clickable(enabled = enabled,')
has('app/src/main/java/com/kmab/gulp/feature/home/HomeScreen.kt',
    'Visible bento slots stay fixed', 'val gridGuideAlpha',
    'SystemClock.uptimeMillis()', 'lastSwapTime >= 85L',
    'BlendMode.DstIn', 'animateItem(')
has('app/src/main/java/com/kmab/gulp/core/navigation/GulpNav.kt',
    'fontSize = 11.5.sp', 'delay(1800)', 'NavGlyph(')
has('app/build.gradle.kts', 'versionCode = 10', 'versionName = "0.6.4-rc5"')
has('docs/DESIGN_CONTRACT.md', 'UI_FEEDBACK_AUDIT.md')
has('docs/UI_FEEDBACK_AUDIT.md', 'True backdrop blur', 'Medical fluid plan')
# No old package remnants or retired renderer should creep back into any source root.
for source in (ROOT/'app/src').rglob('*.kt'):
    assert 'package app.gulp.android' not in source.read_text(), source
assert not list((ROOT/'app/src/main/java/app/gulp/android').rglob('*.kt'))
assert not list((ROOT/'app/src/main/java').rglob('StaticWaterBackdrop.kt'))
assert '/build-key' in (ROOT/'.gitignore').read_text(), 'Signing key must be gitignored'
print('PASS: UI follow-up contracts, source package integrity and documentation')
