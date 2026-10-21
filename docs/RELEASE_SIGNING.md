# gulp release signing and Android identity

Android release application ID: `com.kmab.gulp`. Android debug builds install as `com.kmab.gulp.debug`.
The Kotlin source package, manifest component resolution, namespace, and test packages now use `com.kmab.gulp`.

## Generate a private signing key ONCE

From the repository root in PowerShell, run (adjust the path to your JDK):

```powershell
& "D:/Code/java/jdk-21.0.12.1/bin/keytool.exe" -genkeypair -v -keystore build-key -storetype PKCS12 -alias gulp -keyalg RSA -keysize 3072 -validity 14600
```

Follow the interactive prompts to provide a strong password and certificate details. The file `build-key` will be created at the repository root. Keep this exact file for all future APK updates. Do **not** generate a different key for every release.

Create the local properties file:

```powershell
Copy-Item keystore.properties.example keystore.properties
notepad keystore.properties
```

Set `storePassword` and `keyPassword` to the real values you chose, and `keyAlias=gulp` (the alias used in the keytool command). If keytool only prompted for one password, both values are normally the same. Do not include quotation marks around property values.

Both `build-key` and `keystore.properties` are ignored by Git. The properties file stores passwords in plaintext: protect your workstation, and store a backup of the key plus credentials separately in a secure password manager or encrypted offline storage. Never upload them to GitHub or share them in a chat. `.gitignore` does not remove files that were already committed; if secrets were previously committed, revoke/rotate and remove them from history.

## Build

```powershell
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug -Dorg.gradle.java.home="D:/Code/java/jdk-21.0.12.1"
./gradlew :app:assembleRelease :app:bundleRelease -Dorg.gradle.java.home="D:/Code/java/jdk-21.0.12.1"
```

A release **requires** both local files and the three values. The build fails with an explanatory message when they're missing; it never silently emits an unsigned release package. Debug builds do not need release credentials. GitHub Actions intentionally runs debug tests/builds only; no signing credentials are placed in CI.

Expected output:
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`

## Verify the signature and preserve upgrade compatibility

```powershell
& "D:/Code/java/jdk-21.0.12.1/bin/keytool.exe" -list -v -keystore build-key -alias gulp
# Replace the SDK path/build-tools version as necessary:
& "D:/Code/android-sdk/build-tools/35.0.0/apksigner.bat" verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
```

Preserve the keystore and alias, keep `applicationId` identical (`com.kmab.gulp`), and increment `versionCode` for each published update. A package installed as `app.gulp.android` **cannot** be updated in-place by the new `com.kmab.gulp` package; it is a different Android application identity, so existing locally stored records don't migrate automatically. A debug installation (`com.kmab.gulp.debug`) also does not update in place to the release ID.

For Google Play App Signing, this key can be used as your upload key if you opt in; Google may manage a distinct app-signing key for store-distributed APKs. Read the Play Console instructions before distributing independently signed APKs alongside Play downloads.

Official Android signing guide: https://developer.android.com/studio/publish/app-signing
