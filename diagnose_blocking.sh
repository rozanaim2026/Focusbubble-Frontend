#!/bin/bash

# Automatically select the first connected device
DEVICE=$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')
if [ -z "$DEVICE" ]; then
    echo "❌ No device found! Connect your phone via ADB."
    exit 1
fi


echo "🔍 BLOCKING DIAGNOSTIC"
echo "======================"
echo ""
echo "📱 Using device: $DEVICE"
echo ""

# Check selected apps
echo "📱 Checking why apps aren't blocking..."
echo ""

echo "1️⃣ How many apps did you select to block?"
adb -s $DEVICE logcat -d | grep -i "blocked.*app" | grep -i "saved\|insert\|added" | tail -5
echo ""

echo "2️⃣ How many apps did service load?"
adb -s $DEVICE logcat -d | grep "Loaded.*blocked apps" | tail -3
echo ""

echo "3️⃣ Is Usage Stats permission granted?"
USAGE=$(adb -s $DEVICE shell appops get com.focusbubble GET_USAGE_STATS 2>/dev/null)
if echo "$USAGE" | grep -q "allow"; then
    echo "✅ Usage Stats: GRANTED"
else
    echo "❌ Usage Stats: NOT GRANTED (This is the problem!)"
    echo ""
    echo "FIX:"
    echo "1. Open Settings on your phone"
    echo "2. Go to: Apps → Special access → Usage access"
    echo "3. Find FocusBubble and enable it"
fi
echo ""

echo "4️⃣ Is Overlay permission granted?"
OVERLAY=$(adb -s $DEVICE shell dumpsys package com.focusbubble | grep "SYSTEM_ALERT_WINDOW.*granted=true")
if [ -n "$OVERLAY" ]; then
    echo "✅ Overlay: GRANTED"
else
    echo "❌ Overlay: NOT GRANTED"
fi
echo ""

echo "5️⃣ Is service currently running?"
SERVICE=$(adb -s $DEVICE shell dumpsys activity services | grep "BlockerService")
if [ -n "$SERVICE" ]; then
    echo "✅ Service: RUNNING"
else
    echo "❌ Service: NOT RUNNING (Start a session first!)"
fi
echo ""

echo "📋 SUMMARY:"
echo "-----------"
echo ""

# Count blocked apps
LOADED=$(adb -s $DEVICE logcat -d | grep "Loaded.*blocked apps" | tail -1 | grep -o '[0-9]\+' | head -1)
if [ -z "$LOADED" ] || [ "$LOADED" = "0" ]; then
    echo "❌ ISSUE: No apps selected/saved!"
    echo "   FIX: Go to Profile → Edit → Block Apps → Select WhatsApp"
elif ! echo "$USAGE" | grep -q "allow"; then
    echo "❌ ISSUE: Usage Stats permission not granted!"
    echo "   FIX: Settings → Apps → Special Access → Usage Access → Enable FocusBubble"
else
    echo "✅ Everything looks good!"
    echo "   Try: Open session → Home → Open WhatsApp → Wait 5 seconds"
fi
echo ""
