#!/bin/bash

# Set your WiFi ADB device here
DEVICE="192.168.31.203:42521"

# Check if the device is connected
if ! adb devices | grep -q "$DEVICE"; then
    echo "❌ Device $DEVICE not found! Check WiFi ADB connection."
    exit 1
fi

echo "🔍 Testing App Blocking - ANDROID 14 FIX"
echo "========================================"
echo ""
echo "📱 Using device: $DEVICE"
echo ""

# Clear logs
echo "🧹 Clearing logs..."
adb -s $DEVICE logcat -c
echo "✅ Logs cleared!"
echo ""

echo "📊 Starting live monitoring..."
echo "-------------------------------------------"
echo "👉 NOW ON YOUR PHONE:"
echo "   1. Open FocusBubble"
echo "   2. Tap 'Start Focus Session'"
echo "   3. Wait 5 seconds"
echo "   4. Press HOME button"
echo "   5. Open a blocked app (Grofers, WhatsApp, etc.)"
echo "   6. 🎉 BLOCK SCREEN SHOULD APPEAR!"
echo ""
echo "Press Ctrl+C to stop monitoring"
echo "-------------------------------------------"
echo ""

# Monitor relevant logs
adb -s $DEVICE logcat | grep --line-buffered -E "BlockerService|FocusSession|BlockOverlay|checkForegroundApp|Blocking:"
