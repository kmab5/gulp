#!/usr/bin/env python3
"""Offline preflight for known native-compile failures; not a substitute for Gradle."""
from pathlib import Path
root = Path(__file__).resolve().parent.parent
src = root/'app/src/main/java/app/gulp/android'
water = (src/'core/graphics/WaterAdaptiveContent.kt').read_text()
assert 'clipPath(path) { this@drawWithContent.drawContent() }' in water
assert 'clipPath(path) { drawContent() }' not in water
for f in ('DrinkEditorSheet.kt', 'TileEditorSheet.kt'):
    data = (src/'feature/home'/f).read_text()
    assert 'import androidx.compose.material3.ExperimentalMaterial3Api' in data, f
    assert '@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun' in data, f
    assert 'ModalBottomSheet(' in data, f
    assert 'rememberModalBottomSheetState(' in data, f
build = (root/'app/build.gradle.kts').read_text()
assert 'jvmTarget.set(JvmTarget.JVM_17)' in build
assert 'kotlinOptions' not in build
print('PASS: explicit Compose draw scope, Material3 opt-ins, and Kotlin compilerOptions preflight')
