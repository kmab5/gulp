#!/bin/sh
# gulp bootstrap wrapper for environments where the standard Gradle wrapper JAR
# couldn't be bundled. Fetches official v8.13.0 wrapper from Gradle's repository.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_URL="https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradle/wrapper/gradle-wrapper.jar"
EXPECTED_GIT_BLOB="9bbc975c742b298b441bfb90dbc124400a3751b9"
if [ ! -f "$WRAPPER_JAR" ]; then
  command -v git >/dev/null || { echo 'git is needed to verify the Gradle wrapper download' >&2; exit 1; }
  TMP_JAR="$WRAPPER_JAR.tmp.$$"
  trap 'rm -f "$TMP_JAR"' EXIT INT TERM
  if command -v curl >/dev/null; then
    curl --fail --location --retry 2 --silent --show-error "$WRAPPER_URL" -o "$TMP_JAR"
  elif command -v wget >/dev/null; then
    wget -q "$WRAPPER_URL" -O "$TMP_JAR"
  else
    echo 'curl or wget required for wrapper bootstrap' >&2; exit 1
  fi
  ACTUAL=$(git hash-object "$TMP_JAR")
  [ "$EXPECTED_GIT_BLOB" = "$ACTUAL" ] || { echo 'Gradle wrapper blob checksum mismatch' >&2; exit 1; }
  mv "$TMP_JAR" "$WRAPPER_JAR"
  trap - EXIT INT TERM
fi
exec java -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
