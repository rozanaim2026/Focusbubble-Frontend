# ⚡ QUICK START - Testing the Fixed App

## 🎯 IMMEDIATE TEST (30 seconds)

### Open Terminal and run:

```bash
cd /Users/apple/Downloads/Final-Major-Project
./test_and_capture.sh
```

**Then on your phone:**
1. Tap "Start Focus Session"
2. Watch what happens

**The script will:**
- ✅ Auto-detect if it works
- ❌ Auto-save crash logs if it crashes

---

## 📱 MANUAL TEST (If you prefer)

### Step 1: Open App
```
1. Look at your phone
2. Open FocusBubble
```

### Step 2: Try Starting Session
```
1. Tap "Start Focus Session"
```

### Step 3: Expected Results

**If Working (Good!):**
```
✅ Permission dialog appears
✅ OR session screen opens
✅ NO crash!
✅ NO "keeps stopping" error!
```

**If Still Crashing (Need help!):**
```
❌ App closes immediately
❌ Shows "FocusBubble keeps stopping"
❌ Run: ./test_and_capture.sh
❌ Send me the crash_report.txt file
```

---

## 🔧 IF IT CRASHES - GET LOGS

### Option 1: Automated (Easiest)
```bash
cd /Users/apple/Downloads/Final-Major-Project
./test_and_capture.sh
```
Then tap the button and it will capture everything!

### Option 2: Manual
```bash
cd /Users/apple/Downloads/Final-Major-Project

# Clear logs
adb logcat -c

# Monitor logs  
adb logcat > crash.txt
```

Then tap button, if it crashes press Ctrl+C

```bash
# Get crash info
grep -A 50 "FATAL" crash.txt
```

---

## ✅ WHAT I FIXED

### 3 Critical Bugs:
1. ✅ **Database crash** - Room was on main thread
2. ✅ **Service crash** - Wrong notification intent
3. ✅ **No error messages** - Added Toast messages

### All Permissions:
1. ✅ Notification permission (Android 13+)
2. ✅ Overlay permission
3. ✅ Usage stats permission
4. ✅ Foreground service
5. ✅ Internet
6. ✅ Query all packages
7. ✅ Network state

---

## 🎊 SUCCESS INDICATORS

### The app is working if you see:

```
✅ Permission dialog appears (not crash)
✅ Session screen opens
✅ Timer starts counting
✅ Notification shows
✅ Can open app and tap button multiple times
```

### Still have problems if you see:

```
❌ App closes immediately
❌ "FocusBubble keeps stopping" dialog
❌ Black screen then crash
```

---

## 📞 NEED HELP?

Run this and send me the output:

```bash
cd /Users/apple/Downloads/Final-Major-Project
./test_and_capture.sh
```

After crash, send me:
- `crash_report.txt` file
- What you saw on screen
- When exactly it crashed

---

**TRY IT NOW! The app should work without crashing!** 🚀
