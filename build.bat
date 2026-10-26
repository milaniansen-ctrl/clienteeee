@echo off
setlocal
cd /d "%~dp0"
where gradle >nul 2>nul
if errorlevel 1 (
  echo ERROR: Gradle is not installed or is not on PATH.
  echo Install Gradle 8.14 or newer and Java 21, then run this file again.
  echo Project targets Minecraft 1.21.11 with Fabric.
  pause
  exit /b 1
)
java -version 2>&1 | findstr /C:"21." >nul
if errorlevel 1 echo WARNING: This project requires Java 21. Make sure Gradle uses Java 21.
gradle clean build
if errorlevel 1 (
  echo.
  echo BUILD FAILED. Read the Gradle error above. Check Java 21 and internet access.
  pause
  exit /b 1
)
echo.
echo BUILD SUCCEEDED. Your mod jar is in build\libs\
explorer "%~dp0build\libs"
pause
