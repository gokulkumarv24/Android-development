@echo off
echo ===========================================
echo   EdgeDetector App - Environment Setup
echo ===========================================
echo.

REM Set Android SDK path
set ANDROID_HOME=C:\Users\C19759\AppData\Local\Android\Sdk

REM Add tools to PATH for this session
set PATH=%ANDROID_HOME%\platform-tools;%PATH%
set PATH=%ANDROID_HOME%\tools;%PATH%
set PATH=%ANDROID_HOME%\build-tools\34.0.0;%PATH%
set PATH=%ANDROID_HOME%\cmake\4.1.1\bin;%PATH%

echo Testing development tools:
echo.

echo [1/6] Testing Java:
java --version
echo.

echo [2/6] Testing Git:
git --version
echo.

echo [3/6] Testing ADB:
adb version
echo.

echo [4/6] Testing CMake:
cmake --version
echo.

echo [5/6] Testing Node.js:
node --version 2>nul || echo "Node.js not installed - download from https://nodejs.org/"
npm --version 2>nul || echo "NPM not installed"
echo.

echo [6/6] Testing Android devices:
adb devices
echo.

echo ===========================================
echo   Environment Setup Complete!
echo ===========================================
echo.
echo You can now run EdgeDetector development commands.
echo To build the project, run: build-project.bat
echo.
pause