#!/usr/bin/env python3
"""Report actual release outputs. These are compressed build artifacts, not per-device Play download estimates."""
from pathlib import Path
root = Path(__file__).resolve().parents[1]
paths = [root / 'app/build/outputs/apk/debug', root / 'app/build/outputs/apk/release', root / 'app/build/outputs/bundle/release']
found = 0
for folder in paths:
    for artifact in sorted(folder.glob('*')):
        if artifact.suffix.lower() not in {'.apk','.aab'}:
            continue
        found += 1
        mib = artifact.stat().st_size / (1024 * 1024)
        print(f'{artifact.relative_to(root)}: {mib:.2f} MiB')
        if artifact.suffix.lower() == '.aab' and mib > 20:
            print('  SIZE REVIEW: over 20 MiB compressed AAB, investigate before publishing')
if not found:
    raise SystemExit('No APK/AAB artifacts to measure: run assembleDebug / bundleRelease first')
