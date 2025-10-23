#!/bin/bash

echo "📱 Connecting Your Phone via WiFi"
echo "================================="
echo ""

# Your details
IP="192.168.31.203"
PAIRING_PORT="36317"
CODE="670037"

echo "🔄 Step 1: Pairing with your phone..."
echo "IP: $IP"
echo "Pairing Port: $PAIRING_PORT"
echo "Code: $CODE"
echo ""

# Pair
adb pair $IP:$PAIRING_PORT <<EOF
$CODE
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Pairing successful!"
    echo ""
    echo "-------------------------------------------"
    echo "📱 NOW ON YOUR PHONE:"
    echo "   1. Press BACK button (go back one screen)"
    echo "   2. Look at the top - you'll see:"
    echo "      'IP address & Port'"
    echo "      192.168.31.203:XXXXX"
    echo "   3. Tell me that port number (XXXXX)"
    echo "-------------------------------------------"
    echo ""
    read -p "Enter the MAIN port number: " MAIN_PORT
    
    echo ""
    echo "🔄 Step 2: Connecting to device..."
    adb connect $IP:$MAIN_PORT
    
    echo ""
    echo "✅ Connection status:"
    adb devices
    echo ""
    echo "🎉 If you see '$IP:$MAIN_PORT    device' above, you're connected!"
else
    echo ""
    echo "❌ Pairing failed!"
    echo ""
    echo "Try this manually:"
    echo "  adb pair $IP:$PAIRING_PORT"
    echo "  (When prompted, enter: $CODE)"
fi
