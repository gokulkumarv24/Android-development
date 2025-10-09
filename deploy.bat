@echo off
echo ============================================
echo EdgeDetectorApp - Complete Deployment Guide
echo ============================================
echo.

echo STEP 1: Starting Web Viewer Server...
echo ----------------------------------------
cd web
start cmd /k "npm run serve"
cd ..
echo ✅ Web viewer starting at http://localhost:8080
echo.

echo STEP 2: Android Device Instructions
echo ----------------------------------------
echo 1. Connect your Android device via USB
echo 2. Enable USB Debugging in Developer Options
echo 3. Allow USB Debugging when prompted
echo.

echo STEP 3: Build and Deploy Android App
echo ----------------------------------------
echo Building Android APK...
call gradlew.bat assembleDebug
echo.

echo STEP 4: Manual Deployment (if needed)
echo ----------------------------------------
echo If build successful, APK location:
echo app\build\outputs\apk\debug\app-debug.apk
echo.
echo To install manually:
echo adb install app\build\outputs\apk\debug\app-debug.apk
echo.

echo ============================================
echo NEXT STEPS:
echo ============================================
echo 1. Open http://localhost:8080 in your browser
echo 2. Install and run the app on your device
echo 3. Grant camera permissions when prompted
echo 4. Watch real-time edge detection!
echo ============================================
echo.
pause