#!/usr/bin/env python3
"""Source contracts for current UI repair. Does not substitute for Android compiler/device testing."""
from pathlib import Path
import sys
root=Path(__file__).resolve().parents[1]
def read(p):return (root/p).read_text(encoding='utf-8')
glass=read('app/src/main/java/com/kmab/gulp/core/design/GlassEffects.kt')
nav=read('app/src/main/java/com/kmab/gulp/core/navigation/GulpNav.kt')
home=read('app/src/main/java/com/kmab/gulp/feature/home/HomeScreen.kt')
onboard=read('app/src/main/java/com/kmab/gulp/feature/onboarding/OnboardingScreen.kt')
settings=read('app/src/main/java/com/kmab/gulp/feature/settings/SettingsScreen.kt')
workflow=read('.github/workflows/android-ci.yml')
checks={
 'Android action v4': 'android-actions/setup-android@v4' in workflow and 'setup-android@v3' not in workflow,
 'actual blurred water': '.blur(blurDp.dp' in glass and 'WaterMath.surfaceY(' in glass,
 'conditional top edge': 'canScrollBack()' in glass,
 'conditional bottom edge': 'canScrollForward()' in glass,
 'screen blur shared': all('GlassScrollableColumn' in read('app/src/main/java/com/kmab/gulp/feature/'+p) for p in ['history/HistoryScreen.kt','reminders/RemindersScreen.kt','review/ReviewScreen.kt','settings/SettingsScreen.kt','onboarding/OnboardingScreen.kt']),
 'home blur conditional': 'conditionalScrollBlur' in home and 'rememberGraphicsLayer()' in glass,
 'navbar transparent': 'GlassSurface(' in nav and 'Brush.radialGradient' not in nav,
 'water-aware icon glyphs': 'NavGlyph(tab, scene, phase.value, selectedTab, Color.White,' in nav and 'waterBelowClip' in nav and 'waterAboveClip' in nav,
 'nav glyph wobble': 'sin(scene.seconds' in nav,
 'animated onboarding wave': 'wavePhase' in onboard and 'WaterAdaptiveSubmark(scene, viewport' in onboard,
 'home tile glass': 'GlassSurface(modifier' in home,
 'feedback entry': 'https://kmab5.github.io/kmab-feedback/' in settings and '"gulp"' in settings,
 'about entry': 'About gulp' in settings and 'github.com/kmab5/gulp' in settings,
 'two-sided text masking': 'waterAboveClip' in read('app/src/main/java/com/kmab/gulp/core/graphics/WaterAdaptiveContent.kt'),
}
for name,ok in checks.items(): print(('PASS' if ok else 'FAIL'),name)
if not all(checks.values()):sys.exit(1)
