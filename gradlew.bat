@echo off
setlocal
set "APP_HOME=%~dp0"
set "JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
if not exist "%JAR%" (
  echo Downloading verified official Gradle 8.13 wrapper...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$u='https://services.gradle.org/distributions/gradle-8.13-wrapper.jar'; $p='%JAR%'; $tmp=$p+'.download'; try { Invoke-WebRequest -Uri $u -OutFile $tmp -UseBasicParsing; $expected=((Invoke-WebRequest -Uri ($u+'.sha256') -UseBasicParsing).Content).Trim().ToLower(); $actual=(Get-FileHash -Algorithm SHA256 $tmp).Hash.ToLower(); if ($expected -ne $actual) { throw 'Checksum mismatch' }; Move-Item -Force $tmp $p } catch { Remove-Item $tmp -ErrorAction SilentlyContinue; Write-Error $_; exit 1 }"
  if errorlevel 1 exit /b 1
)
java -Dorg.gradle.appname=gradlew -classpath "%JAR%" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
