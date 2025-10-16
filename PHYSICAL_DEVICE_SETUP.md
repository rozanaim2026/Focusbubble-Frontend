# 📱 Physical Device Setup - FIXED!

## ✅ What I Changed

Updated both RetrofitClient files to use your **Mac's IP address**:

```kotlin
private const val BASE_URL = "http://192.168.31.161:8000/"
```

This replaces `10.0.2.2` which only works for emulators.

---

## ⚠️ IMPORTANT: Both Devices Must Be on Same WiFi

Your phone and Mac **MUST** be connected to the **same WiFi network** for this to work.

**Check this:**
1. **Mac WiFi**: Open System Preferences → Network → Check WiFi name
2. **Phone WiFi**: Settings → WiFi → Check connected network
3. **Must match!** If different, connect to the same network

---

## 🚀 Testing Steps

### Step 1: Verify Backend is Running

On your Mac, check the backend terminal shows:
```
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
```

✅ **Already running!** (I checked for you)

### Step 2: Test from Mac (Sanity Check)

On your Mac terminal:
```bash
curl http://192.168.31.161:8000/health
```

Expected: `{"ok":true,"time":"..."}`

### Step 3: Rebuild Your Android App

In Android Studio:
1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**
3. **Run** the app on your phone

### Step 4: Try Sign-In

1. Click **Sign in with Google**
2. Complete the sign-in
3. **Watch your Mac terminal** for logs!

---

## 📊 What to Look For

### On Your Mac Terminal (Backend):

**Success looks like:**
```
INFO:     192.168.31.203:XXXXX - "POST /auth/google HTTP/1.1" 200 OK
🔵 Verifying Google token (length: 1234)
🔑 Expected client ID: 464315770315...
✅ Token verified successfully!
📧 Email: your@email.com
```

**Failure looks like:**
```
❌ Token verification failed: ...
```

### On Android Logcat (Filter: GoogleSignIn):

**Success:**
```
📤 Sending token to backend...
📡 Response code: 200
✅ Token sent successfully
```

**Failure:**
```
❌ Failed to send token to backend
Connection refused / Timeout
```

---

## 🐛 Troubleshooting

### "Connection Refused" or Timeout

**Cause**: Your phone can't reach your Mac

**Fixes:**
1. ✅ Check both on **same WiFi**
2. ✅ Verify Mac IP: `ipconfig getifaddr en0` (should be `192.168.31.161`)
3. ✅ Backend running with `--host 0.0.0.0` ✅ (Already done!)
4. Check Mac **Firewall**:
   - System Preferences → Security & Privacy → Firewall
   - If enabled, click "Firewall Options"
   - Ensure Python/uvicorn is allowed

### "Backend Sync Failed" (401 Error)

**Cause**: Token verification failed

**Possible reasons:**
1. Backend can't reach Google servers (check Mac internet)
2. Wrong Google Client ID (already verified ✅)
3. Token expired (try again)

### Different WiFi Networks?

If you can't connect both to same WiFi:

**Option 1: Use Mac's Hotspot**
1. Mac: System Preferences → Sharing → Internet Sharing
2. Share connection over WiFi
3. Connect phone to Mac's hotspot
4. **Update IP** in RetrofitClient to Mac's hotspot IP (usually `172.20.10.1`)

**Option 2: Use USB Debugging**
1. Connect phone via USB
2. Enable USB debugging
3. Use `adb reverse tcp:8000 tcp:8000`
4. Change BASE_URL to `http://localhost:8000/`

---

## 🔧 Quick Reference

### Your Current Setup:
- **Mac IP**: `192.168.31.161`
- **Phone IP**: `192.168.31.203` (via WiFi debugging)
- **Backend Port**: `8000`
- **Backend URL**: `http://192.168.31.161:8000/`

### Commands:

**Check Mac IP:**
```bash
ipconfig getifaddr en0
```

**Check Backend Running:**
```bash
lsof -i :8000
```

**Test Backend from Mac:**
```bash
curl http://192.168.31.161:8000/health
```

---

## ✅ Next Steps

1. **Make sure both devices on same WiFi** ⚠️ CRITICAL
2. **Rebuild** your Android app in Android Studio
3. **Run** on your phone
4. **Sign in** with Google
5. **Watch logs** on Mac terminal

The "backend sync failed" should now be **"✅ Welcome [Your Name]!"** 🎉

---

## 📞 Still Having Issues?

If it still fails, tell me:
1. Are phone and Mac on the same WiFi? (WiFi names?)
2. What does Mac terminal show when you sign in?
3. What does Android Logcat show? (Response code?)
4. Can you run `curl http://192.168.31.161:8000/health` from Mac?

I'm here to help! 🚀
