#!/usr/bin/env python3
"""Catch invalid Gradle version-catalog aliases before Gradle configuration starts.

This is a lightweight offline preflight, *not* a substitute for Gradle sync.
"""
from pathlib import Path
import re
import sys
import tomllib

ROOT = Path(__file__).resolve().parents[1]
path = ROOT / "gradle" / "libs.versions.toml"
catalog = tomllib.loads(path.read_text(encoding="utf-8"))
valid = re.compile(r"[a-z][a-zA-Z0-9_.-]+\Z")
errors = []
for section in ("versions", "libraries", "plugins", "bundles"):
    for alias in catalog.get(section, {}):
        if not valid.fullmatch(alias):
            errors.append(f"Invalid {section} alias {alias!r} (must start lowercase, use letters/digits/_/-/.)")
versions = catalog.get("versions", {})
for section in ("libraries", "plugins"):
    for alias, entry in catalog.get(section, {}).items():
        if not isinstance(entry, dict):
            continue
        ref = entry.get("version", {})
        if isinstance(ref, dict):
            name = ref.get("ref")
            if name is not None and name not in versions:
                errors.append(f"Undefined version reference in {section}.{alias}: {name!r}")
if catalog.get("plugins", {}).get("ksp", {}).get("version", {}).get("ref") != "ksp":
    errors.append("The KSP plugin should reference the lowercase 'ksp' version alias")
if errors:
    print("FAIL: Gradle catalog preflight")
    for error in errors: print(" - " + error)
    sys.exit(1)
print(f"PASS: catalog preflight ({sum(len(catalog.get(x, {})) for x in ('versions','libraries','plugins'))} aliases; references resolved)")
