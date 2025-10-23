# 🔍 DEBUG BLOCKING - Find Why Apps Aren't Being Blocked

## 🎯 PROBLEM: Session starts but apps NOT blocking!

Let's debug step by step.

---

## ✅ STEP 1: Check if Apps are Selected

Run this:
```bash
adb shell "cd /data/data/com.focusbubble/databases && ls -la"
```

This shows if the database exists.

**Expected:** Should show `blocked_apps_db` file

---

## ✅ STEP 2: Check Service is Running

While session is active, run:
```bash
adb shell dumpsys activity services com.focusbubble | grep -A 20 BlockerService
```

**Expected:** Should show service details with "app=ProcessRecord"

---

## ✅ STEP 3: Monitor Blocking Logs

Run this script:
```bash
cd /Users/apple/Downloads/Final-Major-Project
./test_blocking.sh
```

Then:
1. On phone: Start focus session
2. Wait 5 seconds
3. Press Home
4. Open a blocked app (e.g., WhatsApp)
5. Watch the logs

**Expected logs:**
```
BlockerService: Loaded X blocked apps from database
BlockerService: Blocking: WhatsApp (com.whatsapp)
BlockerService: Blocked app detected: com.whatsapp
BlockOverlay: Showing block screen
```

**If you see "Loaded 0 blocked apps":**
→ Apps aren't being saved to database! (Issue #1)

**If you see "Loaded 1 blocked apps" but no "Blocked app detected":**
→ Service not detecting foreground app! (Issue #2)

**If you see "Blocked app detected" but no overlay:**
→ Overlay permission issue! (Issue #3)

---

## ✅ STEP 4: Check All Permissions

```bash
# Check overlay permission
adb shell settings get secure overlay_apps_allowed

# Check usage stats
adb shell appops get com.focusbubble GET_USAGE_STATS

# Check notification
adb shell dumpsys notification | grep com.focusbubble
```

**Expected:**
- Overlay: Should NOT return "null"
- Usage stats: Should return "allow"
- Notification: Should show allowed

---

## 🔧 COMMON ISSUES & FIXES

### Issue #1: Apps Not Saved to Database

**Symptom:** Logs show "Loaded 0 blocked apps"

**Cause:** Apps aren't being saved when you select them

**Fix needed:** Check BlockedAppsViewModel save logic

---

### Issue #2: Service Not Detecting Apps

**Symptom:** Logs show "Loaded 1 blocked apps" but no "Blocked app detected"

**Possible causes:**
- Usage Stats permission not granted
- Service not monitoring (loop not running)
- Package name mismatch

**Debug:** Check logs for:
```
Cannot check foreground app - no Usage Stats permission
```

---

### Issue #3: Overlay Not Showing

**Symptom:** Logs show "Blocked app detected" but screen doesn't appear

**Possible causes:**
- Overlay permission not granted
- BlockOverlayActivity not working
- Activity not in manifest

**Debug:** Check if overlay permission granted:
```bash
adb shell dumpsys package com.focusbubble | grep SYSTEM_ALERT_WINDOW
```

Should show "granted=true"

---

## 🚀 QUICK TEST SEQUENCE

### Test 1: Select an App

1. Open FocusBubble
2. Tap Profile icon
3. Tap "Edit"
4. Tap "Block Apps"
5. Select WhatsApp
6. Tap "Confirm (1 selected)"

**Run this immediately:**
```bash
adb logcat -d | grep -E "BlockedApp|ViewModel|Database" | tail -30
```

Should show logs about saving to database.

---

### Test 2: Start Session & Monitor

```bash
./test_blocking.sh
```

Then:
1. Tap "Start Focus Session"
2. Wait for logs showing "Loaded X blocked apps"
3. Check the number is > 0
4. Press Home
5. Open WhatsApp
6. Wait 2-5 seconds
7. Watch logs

---

### Test 3: Check Permissions

```bash
# All at once
echo "=== OVERLAY ==="
adb shell dumpsys package com.focusbubble | grep SYSTEM_ALERT_WINDOW

echo -e "\n=== USAGE STATS ==="
adb shell appops get com.focusbubble GET_USAGE_STATS

echo -e "\n=== NOTIFICATION ==="
adb shell dumpsys notification | grep -A 3 com.focusbubble | head -5
```

All should show "granted" or "allow"

---

## 📊 EXPECTED COMPLETE LOG FLOW

When blocking works correctly:

```
[SELECT APP]
BlockedAppsViewModel: Adding app: WhatsApp (com.whatsapp)
Database: Inserted blocked app
BlockedAppsViewModel: Successfully saved 1 apps

[START SESSION]
FocusSession: Starting BlockerService
BlockerService: Service created
BlockerService: Service started with duration: 25 minutes
BlockerService: Loaded 1 blocked apps from database
BlockerService: Blocking: WhatsApp (com.whatsapp)

[OPEN WHATSAPP]
BlockerService: Checking foreground app...
BlockerService: Foreground package: com.whatsapp
BlockerService: Blocked app detected: com.whatsapp
BlockerService: Launching block overlay
BlockOverlay: onCreate - Showing block screen for com.whatsapp
```

---

## 🎯 MOST LIKELY ISSUES

Based on "session starts but no blocking":

### 1. Apps Not Being Saved (Most Likely!)
**Test:**
```bash
# After selecting WhatsApp
adb logcat -d | grep -i "blocked" | tail -20
```

**Should see:** "Successfully saved 1 apps" or similar

**If not:** ViewModel isn't saving to database

---

### 2. Usage Stats Permission Not Actually Granted
**Test:**
```bash
adb shell appops get com.focusbubble GET_USAGE_STATS
```

**Should see:** "GET_USAGE_STATS: allow"

**If "default":** Permission not granted!

**Fix:** Go to Settings → Apps → Special Access → Usage Access → Enable for FocusBubble

---

### 3. Service Not Monitoring
**Test:**
```bash
adb logcat | grep "checkForegroundApp"
```

**Should see:** Log every 2 seconds while session active

**If not:** Monitoring loop not running

---

## 🚨 RUN THIS NOW

Copy and run this complete diagnostic:

```bash
#!/bin/bash
echo "🔍 COMPLETE BLOCKING DIAGNOSTIC"
echo "================================"
echo ""

echo "1️⃣ Checking database..."
adb shell "ls -la /data/data/com.focusbubble/databases/ 2>/dev/null" || echo "❌ Database not accessible"
echo ""

echo "2️⃣ Checking service..."
adb shell dumpsys activity services com.focusbubble | grep -q BlockerService && echo "✅ Service registered" || echo "❌ Service not found"
echo ""

echo "3️⃣ Checking permissions..."
echo "Overlay: $(adb shell dumpsys package com.focusbubble | grep SYSTEM_ALERT_WINDOW | grep granted)"
echo "Usage: $(adb shell appops get com.focusbubble GET_USAGE_STATS)"
echo ""

echo "4️⃣ Recent relevant logs..."
adb logcat -d | grep -E "BlockerService|BlockedApp" | tail -15
echo ""

echo "✅ Diagnostic complete!"
echo ""
echo "NOW: Start session and open WhatsApp, then run ./test_blocking.sh"
```

Save this as `diagnose.sh`, chmod +x it, and run it!

---

**START WITH: `./test_blocking.sh` and tell me what logs you see!**
