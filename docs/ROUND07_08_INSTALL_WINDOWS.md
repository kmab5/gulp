# gulp — Combined Round 07 + Round 08 Windows-compatible update

This update combines the **General Round 07** and **All Screens Round 08** deliveries in a single step.
It targets GitHub commit `f5b91e6235b2d0358831743fd7247086dc8944a1`
(the repository before either update). It does **not** require the two older installers.

## Root cause of the previous failures

Windows Git checkouts converted LF to CRLF in several Kotlin and shell files. The old
Round 07 Python installer compared **raw file bytes** against LF-only checksums,
and incorrectly treated the CRLF copies as modified files. For example:

- `GulpDatabase.kt`: expected LF checksum `fd38ab5287d057b2...`, Windows CRLF
  checksum `73c10c77a4db0d5e...`. These are the same source content.
- `GulpRepository.kt`: LF `88b3e9937ec339e1...`, CRLF `0a5ddd5345619e7e...`.
- `tools/verify_phase3.sh`: LF `9dd98f38341a58c...`, CRLF
  `37671eee2a7c3542...`.

The combined installer normalizes CRLF/LF **for comparisons** and writes LF-normalized
UTF-8 source consistently. It also accepts a repository where some files already
contain Round 07 or Round 08 changes. It does not overwrite genuine unrelated edits.

## Apply in PowerShell

1. Extract this ZIP outside the repository, for example `D:\Downloads\gulp-07-08-fixed`.
2. Make sure the working tree is backed up or your changes are committed (`git status --short`).
3. From any directory, run:

```powershell
py D:\Downloads\gulp-07-08-fixed\apply_update.py D:\Code\gulp --check
py D:\Downloads\gulp-07-08-fixed\apply_update.py D:\Code\gulp
```

**Don't run the two older installers or the two older Git patches.**
The installer validates every target before writing anything. A backup directory
is created next to `apply_update.py` during application. If it encounters a
real unrelated edit, it stops and shows the exact file; do not bypass the check.

## Verify

```powershell
cd D:\Code\gulp
py tools/verify_general_round.py
py tools/verify_drink_types.py
.\gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug -Dorg.gradle.java.home="D:/Code/java/jdk-21.0.12.1"
```

The Round 08 tests include a Bash test file, so for WSL/Git Bash use:

```bash
bash tools/verify_round08.sh
```

Some source tests are Kotlin/SDK-independent, but a successful Android build and
device test are still required. Do not assume the APK compiles solely because
the Python checks passed.

## Safety notes

- No database reset or manual SQL commands.
- Do not delete old drink records, `build-key`, or `keystore.properties`.
- No paths are deleted by this update.
- This is an **incremental overlay**, not a complete repository replacement.
- `.gitattributes` is included to prevent recurring Windows line-ending problems.
- Changes to the Home, navbar, Reminders, History, Review and Settings from Round 08
  are included, along with the Round 07 editable drink catalog and migration.
