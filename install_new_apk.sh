#!/bin/bash

echo "📦 Installing NEW APK with Timer & Buttons"
echo "=========================================="
echo ""

# Get connected device
DEVICE=$(adb devices | grep -v "List" | grep "device$" | awk '{print $1}' | head -1)

if [ -z "$DEVICE" ]; then
    echo "❌ No device connected!"
    echo ""
    echo "Please reconnect your device:"
    echo "  1. On phone: Enable Wireless Debugging"
    echo "  2. Run: ./connect_my_phone.sh"
    echo "  OR connect via USB cable"
    exit 1
fi

echo "📱 Found device: $DEVICE"
echo ""

echo "🔄 Installing updated APK..."
adb -s $DEVICE install -r app/build/outputs/apk/debug/app-debug.apk

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ NEW APK INSTALLED!"
    echo ""
    echo "🎉 What's New:"
    echo "  ✅ Persistent blocking (reappears every time!)"
    echo "  ✅ Live timer showing remaining time"
    echo "  ✅ 'Continue with Focus' button (green)"
    echo "  ✅ 'Emergency Use' button (red)"
    echo ""
    echo "📊 Test it now:"
    echo "  1. Start focus session"
    echo "  2. Open WhatsApp"
    echo "  3. See NEW overlay with timer & buttons!"
    echo "  4. Tap 'Continue with Focus'"
    echo "  5. Open WhatsApp AGAIN"
    echo "  6. Overlay REAPPEARS! (Persistent!)"
    echo ""
else
    echo ""
    echo "❌ Installation failed!"
    echo ""
    echo "Try:"
    echo "  1. Unlock your phone"
    echo "  2. Make sure device is connected"
    echo "  3. Run this script again"
fi
