# 📱 Connect Physical Device via WiFi (Wireless Debugging)

## 🎯 STEP 1: Enable Wireless Debugging on Your Phone

### On Your Android Phone:

1. Open **Settings**
2. Go to **Developer Options**
   - If you don't see it: Settings → About Phone → Tap "Build Number" 7 times
3. Scroll down to find **Wireless Debugging**
4. **Enable** the toggle
5. Tap on **Wireless Debugging** to open it

---

## 🎯 STEP 2: Get Pairing Code

### Still on Your Phone:

1. In **Wireless Debugging** screen
2. Tap **"Pair device with pairing code"**
3. You'll see a screen with:
   - **Wi-Fi pairing code** (6 digits)
   - **IP address & Port** (e.g., 192.168.1.5:12345)

**Example:**
```
Wi-Fi pairing code: 123456

IP address & Port
192.168.1.5:37453
```

**Keep this screen open!**

---

## 🎯 STEP 3: Pair from Your Mac

### On Your Mac Terminal:

Run this command (replace with YOUR IP and port from step 2):

```bash
adb pair 192.168.1.5:37453
```

**It will ask for pairing code.**

Type the **6-digit code** from your phone and press Enter.

**Example:**
```bash
$ adb pair 192.168.1.5:37453
Enter pairing code: 123456
Successfully paired to 192.168.1.5:37453 [guid=adb-RZ8R20XXXXXX]
```

---

## 🎯 STEP 4: Connect to Device

### Get the Connection Port

**On your phone:**
- Go back to **Wireless Debugging** main screen (back button once)
- Look for **"IP address & Port"** at the top
- It shows something like: `192.168.1.5:12345`

**This port is DIFFERENT from the pairing port!**

### Connect from Mac:

```bash
adb connect 192.168.1.5:12345
```

(Use YOUR IP and the **main port**, not the pairing port!)

**Expected:**
```
connected to 192.168.1.5:12345
```

---

## 🎯 STEP 5: Verify Connection

```bash
adb devices
```

**Should show:**
```
List of devices attached
192.168.1.5:12345    device
```

---

## ✅ NOW YOU'RE READY!

### Install the APK:

```bash
cd /Users/apple/Downloads/Final-Major-Project
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Run diagnostics:

```bash
./diagnose_blocking.sh
```

### Test blocking:

```bash
./test_blocking.sh
```

---

## 🔄 QUICK REFERENCE

### Pairing (One-time setup):
```bash
# Get pairing code from phone first!
adb pair <IP>:<PAIRING_PORT>
# Enter 6-digit code when prompted
```

### Connecting (Every time):
```bash
# Use the main port, not pairing port
adb connect <IP>:<MAIN_PORT>
```

### Check connection:
```bash
adb devices
```

### Disconnect:
```bash
adb disconnect <IP>:<PORT>
```

---

## 🚨 TROUBLESHOOTING

### Issue: "failed to authenticate"
**Fix:** Get a new pairing code and try again

### Issue: "cannot connect to <IP>:<PORT>"
**Fix:** 
1. Make sure Mac and phone are on **same WiFi network**
2. Check **Wireless Debugging** is still enabled
3. Try the main port, not pairing port

### Issue: Shows "offline" or "unauthorized"
**Fix:** 
1. On phone: Revoke USB debugging authorizations
2. Get new pairing code
3. Pair again

---

## 📋 COMPLETE SETUP SCRIPT

Save this as `connect_phone.sh`:

```bash
#!/bin/bash

echo "📱 Physical Device WiFi Connection"
echo "=================================="
echo ""
echo "⚠️  BEFORE RUNNING THIS:"
echo "   1. Enable Wireless Debugging on your phone"
echo "   2. Tap 'Pair device with pairing code'"
echo "   3. Keep that screen open"
echo ""
read -p "Ready? Press Enter..."
echo ""

read -p "Enter IP address from your phone: " IP
read -p "Enter PAIRING port (from pairing screen): " PAIR_PORT
read -p "Enter pairing code (6 digits): " CODE

echo ""
echo "🔄 Pairing..."
echo "$CODE" | adb pair $IP:$PAIR_PORT

if [ $? -eq 0 ]; then
    echo "✅ Paired successfully!"
    echo ""
    read -p "Now go back on your phone and enter the MAIN port: " MAIN_PORT
    echo ""
    echo "🔄 Connecting..."
    adb connect $IP:$MAIN_PORT
    
    echo ""
    echo "✅ Connection status:"
    adb devices
else
    echo "❌ Pairing failed. Try again!"
fi
```

---

## 🎯 SUMMARY

**Two ports to remember:**
1. **Pairing Port** - Used ONCE for initial pairing (shown when you tap "Pair device")
2. **Main Port** - Used EVERY TIME to connect (shown on main Wireless Debugging screen)

**Steps:**
1. Phone: Enable Wireless Debugging → Get pairing code
2. Mac: `adb pair IP:PAIRING_PORT` → Enter code
3. Phone: Go back → Get main port
4. Mac: `adb connect IP:MAIN_PORT`
5. Mac: `adb devices` to verify

---

**TELL ME YOUR IP ADDRESS AND PORTS, AND I'LL GIVE YOU THE EXACT COMMANDS!** 📱
