#!/usr/bin/env python3
"""Offline source checks for the water-play HUD and unified sheet glass."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
b=root/'app/src/main/java/com/kmab/gulp'
main=(b/'MainActivity.kt').read_text()
home=(b/'feature/home/HomeScreen.kt').read_text()
scene=(b/'core/graphics/WaterScene.kt').read_text()
control=(b/'core/design/WaterPlayControls.kt').read_text()
touch=(b/'core/graphics/WaterPlayInput.kt').read_text()
for p in ['feature/home/TileEditorSheet.kt','feature/home/DrinkEditorSheet.kt']:
    s=(b/p).read_text()
    assert 'containerColor = Color.Transparent' in s, p
    assert 'dragHandle = null' in s, p
    assert 'GlassSurface(Modifier.fillMaxWidth(), radius = 28' in s, p
    assert 'contentAlignment = Alignment.Center' in s, p
    assert 'GulpPalette.DeepBlue.copy(alpha = .78f)' not in s, p
assert 'waterPlayMode by rememberSaveable' in main
assert 'if (!waterPlayMode)' in main
assert 'GulpNav(current = tab' in main
assert 'waterPlayTouchFeedback(scene, enabled = waterPlayMode' in main
assert 'scene.updateMotionIntensity(if (waterPlayMode) com.kmab.gulp.core.domain.WaterPlayRules.sensitivity' in main
assert 'scene.setPreviewFill(if (waterPlayMode) com.kmab.gulp.core.domain.WaterPlayRules.playFill' in main
assert 'onEnterWaterPlay' in home and 'waterPlayControlAlpha' in home
assert 'fun playDrag(' in scene
assert 'setWaterPlaySettings(' in (b/'core/data/GulpPreferences.kt').read_text()
assert 'waterAboveClip(scene, viewport)' in control and 'waterBelowClip(scene, viewport)' in control
assert 'PointerEventPass.Initial' in touch
assert 'python3 tools/verify_water_play_ui.py' in (root/'.github/workflows/android-ci.yml').read_text()
print('PASS: unified popup glass, HUD toggle, adaptive icons, play controls, full-screen water touch')
