#!/usr/bin/env python3
from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
app=root/'app/src/main'
manifest=ET.parse(app/'AndroidManifest.xml').getroot()
ns='{http://schemas.android.com/apk/res/android}'
widgets=[]
for r in manifest.findall('application/receiver'):
    name=r.get(ns+'name','')
    if 'Widget' in name: widgets.append(name)
for name in ('GulpQuickWidgetReceiver','GulpSummaryWidgetReceiver','GulpWidgetDateReceiver'):
    assert any(name in r for r in widgets), name
for info in ('gulp_quick_widget_info.xml','gulp_summary_widget_info.xml'):
    node=ET.parse(app/'res/xml'/info).getroot()
    assert node.attrib.get(ns+'initialLayout')=='@layout/gulp_widget_loading'
repo=(app/'java/com/kmab/gulp/core/data/GulpRepository.kt').read_text()
assert all(x in repo for x in ('suspend fun deleteAll()', 'suspend fun exportCsv()', 'notifyWidgets()'))
settings=(app/'java/com/kmab/gulp/feature/settings/SettingsScreen.kt').read_text()
assert all(x in settings for x in ('onExport','onDeleteAll','AppearanceToggle','strongerContrast'))
activity=(app/'java/com/kmab/gulp/MainActivity.kt').read_text()
assert 'ActivityResultContracts.CreateDocument' in activity
assert 'showDeleteConfirmation' in activity
assert 'CompositionLocalProvider(LocalStrongerContrast' in activity
print('PASS: Phase 5 widget declarations, layout metadata, settings, SAF export and deletion integration')
