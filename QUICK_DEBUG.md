# 🔧 STEP-BY-STEP DEBUGGING GUIDE

## Current Status Analysis

❌ **WebSocket server not running** (port 8765 not active)
❌ **Android app not processing**

## Most Likely Issues:

### 1. **APP CRASHES ON STARTUP** (Most Common)

**Symptoms**: App opens then immediately closes
**Causes**:

- Missing native library (OpenCV)
- JNI loading error
- Android version compatibility

**SOLUTION**: Check if these files are in the APK:

```
- lib/arm64-v8a/libedge_detector.so
- lib/armeabi-v7a/libedge_detector.so
```

### 2. **CAMERA PERMISSION NOT GRANTED**

**Symptoms**: App opens but shows black screen
**SOLUTION**:

1. Settings → Apps → EdgeDetector → Permissions → Camera → ON
2. Or uninstall/reinstall app and tap ALLOW when prompted

### 3. **NETWORK/FIREWALL BLOCKING**

**Symptoms**: App works but web viewer can't connect
**SOLUTION**:

- Windows Firewall: Allow port 8765
- Router: Check if devices are on same network

## SIMPLIFIED TESTING:

### Test 1: Check APK Installation

```
1. Install APK
2. Launch app
3. Does it stay open for more than 2 seconds?
   - YES: Go to Test 2
   - NO: App is crashing (see solutions below)
```

### Test 2: Check Camera Permission

```
1. Launch app (if it stays open)
2. Do you see camera permission dialog?
   - YES: Tap ALLOW, go to Test 3
   - NO: Permission already denied (fix in Settings)
```

### Test 3: Check Camera Feed

```
1. After granting permission
2. Do you see camera preview?
   - YES: Go to Test 4
   - NO: Camera issue (close other camera apps)
```

### Test 4: Check WebSocket Server

```
1. With app running and camera working
2. On computer: Run this command
   netstat -an | findstr 8765
3. Do you see "LISTENING" on port 8765?
   - YES: Server working, check web viewer
   - NO: WebSocket server failed to start
```

## CRASH SOLUTIONS:

### If App Crashes Immediately:

1. **Restart Android device**
2. **Clear app data**: Settings → Apps → EdgeDetector → Storage → Clear Data
3. **Check Android version**: Needs Android 7.0+ (API 24+)
4. **Reinstall APK**

### If Still Crashing:

The issue is likely the native library. Let me create a simpler version without OpenCV:

**Would you like me to create a simplified version without OpenCV to test basic functionality first?**

## QUICK STATUS CHECK:

**Please tell me:**

1. Does the app stay open when you launch it? (Yes/No)
2. Do you see a camera permission dialog? (Yes/No)
3. If you granted permission, do you see camera preview? (Yes/No)
4. Any error messages on screen? (Describe)

This will help me identify the exact issue! 🎯
