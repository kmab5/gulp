#!/usr/bin/env python3
"""Validate Fluid Nucleus brand assets and Home toggle wiring; source-level only."""
from pathlib import Path
import xml.etree.ElementTree as ET
import re
root=Path(__file__).resolve().parents[1]
main=(root/'app/src/main/java/com/kmab/gulp/MainActivity.kt').read_text(encoding='utf-8')
home=(root/'app/src/main/java/com/kmab/gulp/feature/home/HomeScreen.kt').read_text(encoding='utf-8')
mark=(root/'app/src/main/java/com/kmab/gulp/core/graphics/WaterAdaptiveSubmark.kt').read_text(encoding='utf-8')
assert 'showHomeBrandSubmark by rememberSaveable' in main
assert 'showBrandSubmark = showHomeBrandSubmark' in main
assert 'onToggleBrand = {' in main
assert 'AnimatedContent(' in home and 'WaterAdaptiveSubmark(scene, viewport' in home
assert 'onClickLabel = "Switch gulp logo"' in home
assert 'waterBelowClip(scene, viewport)' in mark and 'PathFillType.EvenOdd' in mark
for p in (root/'app/src/main/res/drawable/ic_gulp_foreground.xml',
          root/'app/src/main/res/drawable/ic_gulp_monochrome.xml',
          root/'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml',
          root/'app/src/main/res/values/colors.xml'):
    ET.parse(p)
fg=ET.parse(root/'app/src/main/res/drawable/ic_gulp_foreground.xml').getroot()
mono=(root/'app/src/main/res/drawable/ic_gulp_monochrome.xml').read_text(encoding='utf-8')
assert len(fg.findall('.//path')) == 3
assert 'evenOdd' in mono
assert '@color/launcher_fluid_nucleus' in (root/'app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml').read_text(encoding='utf-8')
for svg in (root/'design/brand').glob('*.svg'): ET.parse(svg)
print('PASS: Fluid Nucleus brand & Home toggle source verification (not Android compilation)')
