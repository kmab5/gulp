#!/usr/bin/env python3
"""SDK-free Round 09 source verification. Native Android build still required."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
app = ROOT / 'app/src/main/java/com/kmab/gulp'
resource = ROOT / 'app/src/main/res/drawable'

def content(path):
    return (app/path).read_text(encoding='utf-8')

checks = {
    'scroll uses transparent conditional feather':
        'BlendMode.DstIn' in content('core/design/GlassEffects.kt')
        and 'blurLayer.renderEffect' not in content('core/design/GlassEffects.kt')
        and 'canScrollBack()' in content('core/design/GlassEffects.kt'),
    'glass is flat': 'Flat frosted treatment' in content('core/design/GlassEffects.kt'),
    'parent observes full-screen swipe':
        'GulpSwipe.nextIndex' in content('MainActivity.kt')
        and 'PointerEventPass.Initial' in content('MainActivity.kt')
        and 'scene.pushFromSwipe(' in content('MainActivity.kt'),
    'water gets directional pushback': 'fun pushFromSwipe(' in content('core/graphics/WaterScene.kt'),
    'bounded drink sheet and shortcut sheet': all(
        'heightIn(max = maxEditorHeight)' in content(f'feature/home/{sheet}.kt')
        and 'verticalScroll(rememberScrollState())' in content(f'feature/home/{sheet}.kt')
        for sheet in ('DrinkEditorSheet', 'TileEditorSheet')),
    'date and time icons':
        'ic_calendar_days' in content('feature/home/DrinkEditorSheet.kt')
        and 'ic_clock' in content('feature/home/DrinkEditorSheet.kt'),
    'reminder icons and spacing':
        'ic_drink_sun' in content('feature/reminders/RemindersScreen.kt')
        and 'ic_drink_moon' in content('feature/reminders/RemindersScreen.kt'),
    'history selection is inverted': 'BlendMode.Clear' in content('core/navigation/GulpNav.kt'),
    'extended catalogue': '"smoothie"' in content('core/domain/DrinkType.kt'),
    'no runtime icon downloads': 'painterResource(iconId)' in content('core/design/DrinkTypeUI.kt'),
    'editor sheet handle is fully backed': all(
        'containerColor = GulpPalette.DeepBlue' in content(f'feature/home/{sheet}.kt')
        for sheet in ('DrinkEditorSheet', 'TileEditorSheet')),
    'icon-only palette, no repeated labels':
        'DrinkIconPicker(selectedId' in content('core/design/NewDrinkTypeDialog.kt')
        and 'DrinkIconPicker(selectedId' in content('core/design/DrinkTypeUI.kt')
        and 'DrinkTypeCatalog.selectableIconIds.forEach' in content('core/design/DrinkTypeUI.kt'),
    'release is updated': 'versionName = "0.7.0"' in (ROOT/'app/build.gradle.kts').read_text(),
}
for label, ok in checks.items():
    assert ok, label
ids = re.search(r'val iconIds = listOf\(([^\n]+)\)',content('core/domain/DrinkType.kt')).group(1)
ids = [part.strip().strip('"') for part in ids.split(',')]
assert len(ids) >= 20
for id in ids:
    path = resource/f'ic_drink_{id}.xml'
    assert path.exists(), f'missing vector for {id}'
    ET.parse(path)
for id in ('calendar_days','clock'):
    ET.parse(resource/f'ic_{id}.xml')
# Compare actual vector geometry rather than treating different names as unique icons.
def geometry(icon_id):
    root = ET.parse(resource / f'ic_drink_{icon_id}.xml').getroot()
    return tuple(node.attrib.get('{http://schemas.android.com/apk/res/android}pathData')
                 for node in root.iter() if '{http://schemas.android.com/apk/res/android}pathData' in node.attrib)
canonical = [i for i in ids if i not in {'leaf', 'soda', 'lemon'}]
assert len(canonical) == len({geometry(i) for i in canonical}), 'duplicate icon shapes in palette'
for hidden, visible in {'leaf':'tea','soda':'cup','lemon':'juice'}.items():
    assert geometry(hidden) == geometry(visible), f'{hidden} alias no longer matches {visible}'
assert (ROOT/'docs/logs/third_party/LUCIDE_ICONS_ISC.md').is_file()
print(f'PASS: {len(checks)} source checks and {len(ids)+2} vector resources')
