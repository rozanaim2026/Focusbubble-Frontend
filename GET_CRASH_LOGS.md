# 🔍 GET CRASH LOGS - HELP ME DEBUG

## 📱 I SEE THE "FOCUSBUBBLE KEEPS STOPPING" ERROR!

The app is crashing. I need to see the error logs to fix it.

---

## 🚀 STEPS TO GET CRASH LOGS

### Step 1: Start Logging (On Your Mac)

Open Terminal and run:

```bash
cd /Users/apple/Downloads/Final-Major-Project
adb logcat > crash_log.txt
```

**Leave this running!** Don't close the terminal.

---

### Step 2: Reproduce the Crash (On Your Phone)

1. Open FocusBubble app
2. Tap "Start Focus Session"
3. Wait for the "FocusBubble keeps stopping" error
4. Tap "Close app"

---

### Step 3: Stop Logging (On Your Mac)

1. Go back to Terminal
2. Press `Ctrl+C` to stop logging
3. The crash is now saved in `crash_log.txt`

---

### Step 4: Find The Error

Run this command:

```bash
grep -A 30 "FATAL EXCEPTION" crash_log.txt
```

**Send me the output!** This will show exactly what's crashing.

---

## 🔍 ALTERNATIVE: Direct Method

If that's too much, just run this ONE command and send me the output:

```bash
adb logcat -d | grep -A 50 "com.focusbubble" | grep -E "FATAL|Exception|Error" | tail -100
```

---

## 💡 WHAT I'M LOOKING FOR

The crash log will show something like:

```
FATAL EXCEPTION: main
Process: com.focusbubble, PID: 12345
java.lang.RuntimeException: ...
    at com.focusbubble...
    at com.focusbubble...
Caused by: ...
```

**This tells me exactly what's wrong!**

---

## 🎯 MOST LIKELY CAUSES

Based on the crash happening when starting session:

### 1. Database Issue
```
Error: Cannot access database
Missing @Database annotation
Table doesn't exist
```

### 2. Service Start Issue
```
Error: Cannot start foreground service
Missing notification channel
Permission denied
```

### 3. Navigation Issue
```
Error: Route not found
NavController not initialized
Invalid destination
```

### 4. Permission Issue
```
Error: Permission denied
Cannot check permission
SecurityException
```

---

## 🚀 QUICK FIX ATTEMPT

While we wait for logs, let's try this:

### Fix 1: Clear App Data

```bash
adb shell pm clear com.focusbubble
```

Then reinstall:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Fix 2: Check Permissions

```bash
adb shell dumpsys package com.focusbubble | grep permission
```

---

## 📱 IN THE MEANTIME

Can you tell me:

1. **When does it crash?**
   - Immediately when you tap the button?
   - After permission dialog?
   - When session screen tries to open?

2. **Did you grant both permissions?**
   - Overlay permission?
   - Usage Stats permission?

3. **Did you select any apps to block?**
   - Yes/No
   - How many?

---

## 🔧 SUSPECTED ISSUE

Looking at your screen, I suspect one of these:

**Most Likely:** Database/Room initialization error
- The app can't access the blocked apps database
- This causes a crash when trying to load blocked apps

**Second Most Likely:** Service can't start
- BlockerService fails to start
- Missing notification channel
- Permission issue

**Third:** Navigation error
- Route not registered
- NavController issue

---

## 💡 EMERGENCY FIX

If you want to try something NOW before we get logs:

```bash
# 1. Completely remove app
adb uninstall com.focusbubble

# 2. Reinstall fresh
adb install app/build/outputs/apk/debug/app-debug.apk

# 3. Grant permissions manually first
adb shell pm grant com.focusbubble android.permission.SYSTEM_ALERT_WINDOW

# 4. Open app and try again
```

---

**Run the logging command and reproduce the crash, then send me the error!** 🔍
