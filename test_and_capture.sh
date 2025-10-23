#!/bin/bash

echo "🔧 FocusBubble - Test & Crash Capture Tool"
echo "=========================================="
echo ""

# Check if device is connected
echo "📱 Checking device connection..."
adb devices | grep -q "device$"
if [ $? -ne 0 ]; then
    echo "❌ No device connected!"
    echo "Please connect your phone/emulator and try again."
    exit 1
fi
echo "✅ Device connected!"
echo ""

# Clear previous logs
echo "🧹 Clearing old logs..."
adb logcat -c
echo "✅ Logs cleared!"
echo ""

# Start the app
echo "🚀 Starting FocusBubble app..."
adb shell am start -n com.focusbubble/.MainActivity
sleep 2
echo "✅ App should be open now!"
echo ""

# Start monitoring logs
echo "📊 Monitoring logs..."
echo "-------------------------------------------"
echo "👉 NOW:"
echo "   1. On your phone: Tap 'Start Focus Session'"
echo "   2. Watch what happens"
echo "   3. Press Ctrl+C here when done"
echo "-------------------------------------------"
echo ""
echo "⏳ Monitoring (press Ctrl+C to stop)..."
echo ""

# Monitor logs
adb logcat | grep --line-buffered -E "FocusBubble|BlockerService|FocusSession|FATAL|AndroidRuntime.*com.focusbubble" | while read line; do
    # Check for fatal errors
    if echo "$line" | grep -q "FATAL"; then
        echo ""
        echo "❌❌❌ CRASH DETECTED! ❌❌❌"
        echo "$line"
        echo ""
        
        # Capture full crash log
        echo "📝 Saving crash log to crash_report.txt..."
        adb logcat -d > crash_report.txt
        
        echo "✅ Crash log saved!"
        echo ""
        echo "📋 Crash summary:"
        grep -A 30 "FATAL" crash_report.txt | head -40
        echo ""
        echo "📁 Full log saved in: crash_report.txt"
        echo ""
        exit 1
    else
        # Print normal logs
        echo "$line"
    fi
done
