#!/usr/bin/env python3
"""Source-level UI contract checks, not an Android/Compose build substitute."""
from pathlib import Path
import xml.etree.ElementTree as ET
root = Path(__file__).resolve().parents[1]
src = root/'app/src/main/java/com/kmab/gulp'
checks = {
    'core/design/GulpComponents.kt': ['fun GulpToggle(', 'fun GulpChoiceChip(', 'fun GulpChoiceRow(', 'fun GulpToggleRow(', 'Brush.linearGradient'],
    'core/navigation/GulpNav.kt': ['delay(1650)', 'NavGlyph(', 'Color.White', 'GulpTab.SETTINGS', 'GulpTab.REVIEW'],
    'feature/onboarding/OnboardingScreen.kt': ['AnimatedContent(', 'AnimatedVisibility(', 'customAmount', 'TimeButton(', 'Theme_Material_Dialog_Alert'],
    'feature/home/HomeScreen.kt': ['LazyVerticalGrid(', '.animateItem(', 'detectDragGesturesAfterLongPress(', 'GlassScrollEdges(homeScroll)', 'onMove =', 'Quick add'],
    'core/graphics/WaterMath.kt': ['* 0.32f'],
    'MainActivity.kt': ['darkColorScheme(', 'MaterialTheme('],
}
for rel, needles in checks.items():
    content = (src/rel).read_text()
    for word in needles:
        assert word in content, f'{rel}: missing {word}'
for rel in ['feature/home/TileEditorSheet.kt', 'feature/home/DrinkEditorSheet.kt',
            'feature/onboarding/OnboardingScreen.kt', 'feature/settings/SettingsScreen.kt',
            'feature/reminders/RemindersScreen.kt', 'MainActivity.kt']:
    content=(src/rel).read_text()
    for legacy in ['FilterChip(', 'Switch(', 'Checkbox(', 'RadioButton(']:
        assert legacy not in content, f'{rel}: legacy Material control {legacy}'
for name in ['ic_gulp_foreground.xml','ic_gulp_monochrome.xml']:
    ET.parse(root/'app/src/main/res/drawable'/name)
ET.parse(root/'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml')
ET.parse(root/'app/src/main/res/values/styles.xml')
print('PASS: gulp UI source contracts and adaptive icon XML')
