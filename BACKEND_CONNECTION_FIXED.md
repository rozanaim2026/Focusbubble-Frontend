# ✅ Backend Connection Fixed!

## 🎯 What Was Wrong

Your Android app had **outdated IP addresses** in the Retrofit configuration files:

### Before (Issues):
1. **RetrofitClient.kt** (data/network/): `http://192.168.1.7:8000/` ❌ (Old IP)
2. **RetrofitClient.kt** (data/api/): `http://192.168.31.203:8000/api/` ❌ (Old IP + wrong /api/ path)

### After (Fixed):
Both files now use: `http://10.0.2.2:8000/` ✅ (Android Emulator)

---

## 🚀 Next Steps

### 1. Make Sure Backend is Running

```bash
cd /Users/apple/focusbubble_backend
source .venv/bin/activate
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

You should see:
```
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
```

### 2. Rebuild Your Android App

In Android Studio:
1. **Build → Clean Project**
2. **Build → Rebuild Project**
3. Run the app on your **Android Emulator**

### 3. Test the Connection

The app should now connect successfully to your backend!

---

## 📱 Different Device Types

### Android Emulator (Current Setup) ✅
```kotlin
private const val BASE_URL = "http://10.0.2.2:8000/"
```
- **Status**: Already configured
- **No changes needed** if using emulator

### Physical Android Device

If you want to test on a **real phone**, change both RetrofitClient files to:

```kotlin
private const val BASE_URL = "http://192.168.31.161:8000/"
```

**Important**: 
- Your computer IP is: `192.168.31.161`
- Phone and computer must be on the **same WiFi network**
- Backend must be running with `--host 0.0.0.0` (not just localhost)

---

## 🔍 How to Verify Backend is Accessible

### From Your Computer:
```bash
curl http://localhost:8000/health
```

Should return: `{"ok":true,"time":"..."}`

### From Android Emulator:
The emulator can access your computer at `10.0.2.2`, so:
- `http://10.0.2.2:8000` = `http://localhost:8000` on your Mac

### From Physical Device:
Use your computer's IP address:
```bash
# Get your IP (already done for you):
ipconfig getifaddr en0
# Returns: 192.168.31.161
```

Then access: `http://192.168.31.161:8000/health`

---

## 🐛 Troubleshooting

### Still Getting "Not Found"?

1. **Check backend is actually running:**
   ```bash
   curl http://localhost:8000/health
   ```

2. **Check Logcat in Android Studio:**
   - Look for HTTP logs from OkHttp
   - Check for "Connection refused" or "Timeout" errors
   - Verify the URL being called

3. **Verify the backend URL in logs:**
   - The HttpLoggingInterceptor will show full request URLs
   - Make sure it's not calling `http://10.0.2.2:8000/api/...` (no `/api/`)

4. **Clean and Rebuild:**
   ```
   Build → Clean Project
   Build → Rebuild Project
   ```

### Connection Timeout?

- Make sure backend is running with `--host 0.0.0.0` not just `localhost`
- Check firewall isn't blocking port 8000
- For physical device: verify both are on same WiFi

---

## 📋 Backend Endpoints Available

Your backend has all these endpoints that your app can now access:

### Health
- `GET /health` - Check backend status

### Authentication
- `POST /auth/google` - Google Sign-In

### Users
- `POST /users` - Create user
- `GET /users/{user_id}` - Get user

### Schedules
- `POST /users/{user_id}/schedules` - Create schedule
- `GET /users/{user_id}/schedules` - List schedules
- `DELETE /users/{user_id}/schedules/{schedule_id}` - Delete schedule

### Sessions
- `POST /users/{user_id}/sessions` - Start session
- `POST /sessions/{session_id}/pause` - Pause session
- `POST /sessions/{session_id}/resume` - Resume session
- `POST /sessions/{session_id}/stop` - Stop session
- `GET /users/{user_id}/sessions/active` - List active sessions

### Blocked Apps
- `POST /users/{user_id}/blocks` - Create blocks
- `GET /users/{user_id}/blocks` - Get active blocks
- `POST /refresh_blocks` - Refresh blocks

---

## ✅ Summary

- ✅ **Backend is running** on port 8000
- ✅ **Retrofit URLs fixed** in both files
- ✅ **Configured for Android Emulator** (10.0.2.2:8000)
- ✅ **Your Mac IP documented** (192.168.31.161) for physical device testing

**Your Android app and backend are now properly synced!** 🎉

Just rebuild the app in Android Studio and it should work.
