#!/bin/bash
echo "🔍 Capturing crash logs for FocusBubble..."
echo ""
echo "📱 Now follow these steps:"
echo "1. On your phone: Open FocusBubble app"
echo "2. Tap 'Start Focus Session' button"
echo "3. Wait for it to crash"
echo "4. Come back here"
echo ""
echo "⏳ Waiting for crash... (monitoring logs)"
echo ""

# Monitor logs for crash
adb logcat | grep -A 100 -E "FATAL|AndroidRuntime.*com.focusbubble|Process.*com.focusbubble.*died"
