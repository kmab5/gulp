#!/bin/sh
# gulp bootstrap. Downloads the official Gradle 8.13 wrapper JAR on first use.
# Once provisioned, executes the standard org.gradle.wrapper.GradleWrapperMain.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://services.gradle.org/distributions/gradle-8.13-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo 'Gradle wrapper JAR missing; retrieving from the official Gradle distribution...' >&2
  TMP_JAR="$WRAPPER_JAR.tmp.$$"
  trap 'rm -f "$TMP_JAR"' EXIT INT TERM
  if command -v curl >/dev/null 2>&1; then
    curl --fail --location --retry 2 --silent --show-error "$WRAPPER_URL" -o "$TMP_JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$WRAPPER_URL" -O "$TMP_JAR"
  else
    echo 'Install curl or wget, or generate the wrapper in Android Studio.' >&2; exit 1
  fi
  # The official Gradle site publishes a SHA256 file for this artifact.
  if command -v sha256sum >/dev/null 2>&1 && command -v curl >/dev/null 2>&1; then
    EXPECTED=$(curl --fail --location --silent --show-error "${WRAPPER_URL}.sha256" | tr -d '\r\n ')
    ACTUAL=$(sha256sum "$TMP_JAR" | cut -d ' ' -f 1)
    if [ "$EXPECTED" != "$ACTUAL" ]; then
      echo 'Official Gradle wrapper checksum mismatch' >&2; exit 1
    fi
  else
    echo 'SHA256 verification requires sha256sum and curl; refusing an unchecked wrapper download.' >&2; exit 1
  fi
  mv "$TMP_JAR" "$WRAPPER_JAR"
  trap - EXIT INT TERM
fi
exec java -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
