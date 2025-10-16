# 🔍 Debugging "Backend Sync Failed" Issue

## ✅ What's Been Fixed

1. **✅ Backend URL corrected** to `http://10.0.2.2:8000/`
2. **✅ Backend logging enhanced** to show detailed token verification steps
3. **✅ Test endpoint added** (`/test/echo`) to verify basic connectivity
4. **✅ Backend restarted** and running on port 8000

---

## 🎯 The Issue

Your app shows: **"Signed in successfully but backend sync failed"**

This means:
- ✅ Google Sign-In works (you get the ID token)
- ❌ Backend can't verify the token

---

## 🔍 Step-by-Step Debugging

### Step 1: Check Backend Logs

When you sign in on your Android app, check the **terminal** where the backend is running.

**You should see logs like:**
```
🔵 Verifying Google token (length: 1234)
🔑 Expected client ID: 464315770315-n5ca91qnc38nmj8039put1ggv22c10n7.apps.googleusercontent.com
```

**If you see:**
- ✅ `✅ Token verified successfully!` → Great! The issue is elsewhere
- ❌ `❌ Token verification failed` → Token issue (see fixes below)
- ❌ Nothing appears → Backend not receiving requests (connectivity issue)

### Step 2: Check Android Logcat

In Android Studio, check **Logcat** and filter for:
- `GoogleSignIn`
- `BackendAuth`
- `RetrofitClient`

**Look for:**
```
📤 Sending token to backend...
📡 Response code: XXX
```

**Response codes:**
- `200` = Success ✅
- `401` = Token verification failed ❌
- `404` = Wrong URL ❌
- `Connection refused` / `Timeout` = Can't reach backend ❌

---

## 🛠️ Possible Fixes

### Fix 1: Verify Basic Connectivity

**Test if Android can reach your backend:**

1. In Android Studio, open **Terminal**
2. Run:
```bash
adb shell
curl http://10.0.2.2:8000/health
```

**Expected response:**
```json
{"ok":true,"time":"2025-..."}
```

If this fails → Backend not accessible from emulator

### Fix 2: Check if Backend Can Reach Google

The backend needs internet to verify tokens with Google's servers.

**Test from your Mac:**
```bash
curl https://oauth2.googleapis.com/tokeninfo
```

If this fails → Your Mac has no internet or firewall is blocking

### Fix 3: Use a Workaround (Temporary)

If token verification keeps failing, you can **temporarily skip verification** for testing:

Edit `/Users/apple/focusbubble_backend/auth.py`:

```python
def verify_google_token(id_token_str: str, client_id: str = None):
    """
    TEMPORARY: Skip verification for testing
    """
    logger.warning("⚠️ SKIPPING TOKEN VERIFICATION - TESTING ONLY!")
    
    # Parse token manually (UNSAFE - only for testing!)
    import json
    import base64
    
    try:
        # Decode JWT payload (middle part)
        parts = id_token_str.split('.')
        if len(parts) != 3:
            raise ValueError("Invalid token format")
        
        payload = parts[1]
        # Add padding if needed
        payload += '=' * (4 - len(payload) % 4)
        decoded = base64.urlsafe_b64decode(payload)
        info = json.loads(decoded)
        
        logger.info(f"✅ Token decoded (UNVERIFIED): {info.get('email')}")
        return info
        
    except Exception as e:
        logger.error(f"❌ Failed to decode token: {e}")
        raise HTTPException(status_code=401, detail=f"Invalid token format: {e}")
```

**⚠️ WARNING:** This skips security checks! Only use for testing, then revert!

---

## 🔧 Fix 4: Ensure Correct Client ID

Your app uses:
```
464315770315-n5ca91qnc38nmj8039put1ggv22c10n7.apps.googleusercontent.com
```

**Verify it's the Web Client ID from your Google Cloud Console:**

1. Go to https://console.cloud.google.com/
2. Select project: `focus-bubble-82576`
3. Go to **APIs & Services** → **Credentials**
4. Find the **Web Client ID** (not Android Client ID!)
5. Make sure it matches the one in your `.env` file

---

## 🧪 Testing the Fix

### Test 1: Basic Connectivity
```bash
# From Android emulator:
adb shell
curl http://10.0.2.2:8000/health
```

### Test 2: Echo Test
```bash
curl -X POST http://10.0.2.2:8000/test/echo \
  -H "Content-Type: application/json" \
  -d '{"test":"hello"}'
```

### Test 3: Try Sign-In Again

1. **Clean and rebuild** your Android app
2. **Sign in** with Google
3. **Watch both**:
   - Backend terminal for logs
   - Android Logcat for errors

---

## 📋 Checklist

- [✅] Backend running on `0.0.0.0:8000`
- [ ] Can curl health endpoint from emulator
- [ ] Backend logs show "Verifying Google token" when signing in
- [ ] Android Logcat shows "Sending token to backend"
- [ ] Response code is 200 (not 401, 404, or connection error)
- [ ] Backend successfully verifies token

---

## 🆘 Still Not Working?

### Collect Debug Info:

1. **Backend logs** (what you see in terminal)
2. **Android Logcat** (filter: GoogleSignIn, BackendAuth)
3. **Response code** from backend
4. **Error message** (if any)

### Common Issues:

**"Connection refused"**
- Emulator can't reach backend
- Wrong URL (should be `10.0.2.2:8000`)
- Backend not running

**"401 Unauthorized"**
- Token verification failed
- Wrong client ID
- Backend can't reach Google servers
- Token expired

**"404 Not Found"**
- Wrong endpoint URL
- Missing `/auth/google` path

---

## ✅ Expected Flow (When Working)

### Android App:
```
1. User signs in with Google
2. Gets ID token from Google
3. Sends token to backend: POST /auth/google
4. Backend responds with user data
5. App saves user and continues
```

### Backend:
```
1. Receives token
2. Verifies with Google servers
3. Extracts email, name, picture
4. Creates/gets user from database
5. Returns user data
```

---

## 📞 Next Steps

1. **Try signing in again**
2. **Watch backend terminal** - do you see verification logs?
3. **Check Logcat** - what's the response code?
4. **Report back** with the logs/errors you see

Your backend is now ready with detailed logging - it will tell us exactly what's going wrong! 🎯
