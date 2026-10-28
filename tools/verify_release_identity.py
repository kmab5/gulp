#!/usr/bin/env python3
"""Offline guard for package migration and release-signing wiring (not a Gradle build)."""
from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[1]
gradle = (ROOT/'app/build.gradle.kts').read_text()
for marker in ('namespace = "com.kmab.gulp"','applicationId = "com.kmab.gulp"','releaseKeyFile = rootProject.file("build-key")',
               'rootProject.file("keystore.properties")','verifyReleaseSigning','signingConfig = signingConfigs.getByName("release")'):
    assert marker in gradle, marker
assert 'fun setReducedMotion(reduced: Boolean)' not in (ROOT/'app/src/main/java/com/kmab/gulp/core/graphics/WaterScene.kt').read_text()
assert 'fun updateReducedMotion(reduced: Boolean)' in (ROOT/'app/src/main/java/com/kmab/gulp/core/graphics/WaterScene.kt').read_text()
assert not (ROOT/'app/src/main/java/app/gulp/android').exists()
for source in (ROOT/'app/src').rglob('*.kt'):
    text = source.read_text()
    assert 'app.gulp.android' not in text, source
    match = re.search(r'^package\s+(\S+)',text,re.M)
    assert match and match.group(1).startswith('com.kmab.gulp'), source
ignore=(ROOT/'.gitignore').read_text()
for name in ('/build-key','/build-key.*','/keystore.properties','.kotlin/','*.jks'):
    assert name in ignore, name
ci=(ROOT/'.github/workflows/android-ci.yml').read_text()
assert ':app:assembleDebug' in ci and ':app:assembleRelease' not in ci
assert (ROOT/'docs/logs/RELEASE_SIGNING.md').is_file()
print('PASS: package migration, Kotlin collision fix, release-signing and ignore contracts')
