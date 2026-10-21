@echo off
setlocal
set "APP_HOME=%~dp0"
set "JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
if not exist "%JAR%" (
  echo Downloading and verifying Gradle 8.13 wrapper JAR...
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$u='https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradle/wrapper/gradle-wrapper.jar'; $p='%JAR%'; $tmp=$p+'.download'; try { Invoke-WebRequest -Uri $u -OutFile $tmp -UseBasicParsing; $b=[IO.File]::ReadAllBytes($tmp); $prefix=[Text.Encoding]::UTF8.GetBytes(('blob ' + $b.Length + [char]0)); $all=New-Object byte[] ($prefix.Length+$b.Length); [Array]::Copy($prefix, 0, $all, 0, $prefix.Length); [Array]::Copy($b, 0, $all, $prefix.Length, $b.Length); $sha=[Security.Cryptography.SHA1]::Create(); $hash=[BitConverter]::ToString($sha.ComputeHash($all)).Replace('-','').ToLowerInvariant(); if ($hash -ne '9bbc975c742b298b441bfb90dbc124400a3751b9') { throw 'Gradle wrapper checksum mismatch' }; Move-Item -Force $tmp $p } catch { Remove-Item $tmp -ErrorAction SilentlyContinue; Write-Error $_; exit 1 }"
  if errorlevel 1 exit /b 1
)
java -Dorg.gradle.appname=gradlew -classpath "%JAR%" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%
