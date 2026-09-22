package com.focusbubble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.provider.Settings
import com.focusbubble.ui.utils.PermissionHelper

@Composable
fun PermissionsCheckDialog(
    onAllPermissionsGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasUsageStats by remember { mutableStateOf(PermissionHelper.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }
    var hasAccessibility by remember { mutableStateOf(PermissionHelper.hasAccessibilityPermission(context)) }
    var hasNotificationListener by remember { mutableStateOf(PermissionHelper.hasNotificationListenerPermission(context)) }
    var showAccessibilityExplainer by remember { mutableStateOf(false) }
    var showNotificationListenerExplainer by remember { mutableStateOf(false) }

    android.util.Log.d("PermissionsDialog", "Dialog created - Overlay: $hasOverlay, Usage: $hasUsageStats, Accessibility: $hasAccessibility, NotificationListener: $hasNotificationListener")

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)

            val previousOverlay = hasOverlay
            val previousUsage = hasUsageStats
            val previousAccessibility = hasAccessibility
            val previousNotificationListener = hasNotificationListener

            hasOverlay = PermissionHelper.hasOverlayPermission(context)
            hasUsageStats = PermissionHelper.hasUsageStatsPermission(context)
            hasAccessibility = PermissionHelper.hasAccessibilityPermission(context)
            hasNotificationListener = PermissionHelper.hasNotificationListenerPermission(context)

            if (hasOverlay != previousOverlay) {
                android.util.Log.d("PermissionsDialog", "Overlay changed: $previousOverlay → $hasOverlay")
            }
            if (hasUsageStats != previousUsage) {
                android.util.Log.d("PermissionsDialog", "Usage changed: $previousUsage → $hasUsageStats")
            }
            if (hasAccessibility != previousAccessibility) {
                android.util.Log.d("PermissionsDialog", "Accessibility changed: $previousAccessibility → $hasAccessibility")
            }
            if (hasNotificationListener != previousNotificationListener) {
                android.util.Log.d("PermissionsDialog", "NotificationListener changed: $previousNotificationListener → $hasNotificationListener")
            }

            if (hasOverlay && hasUsageStats && hasAccessibility && hasNotificationListener) {
                android.util.Log.d("PermissionsDialog", "✅ All permissions granted! Auto-proceeding")
                kotlinx.coroutines.delay(500)
                onAllPermissionsGranted()
                break
            }
        }
    }

    if (showAccessibilityExplainer) {
        AccessibilityExplainerDialog(
            onOpenSettings = {
                showAccessibilityExplainer = false
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            },
            onDismiss = { showAccessibilityExplainer = false }
        )
        return
    }

    if (showNotificationListenerExplainer) {
        NotificationListenerExplainerDialog(
            onOpenSettings = {
                showNotificationListenerExplainer = false
                PermissionHelper.requestNotificationListenerPermission(context)
            },
            onDismiss = { showNotificationListenerExplainer = false }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1C),
        title = {
            Text(
                "Permissions Required",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val allGranted = hasOverlay && hasUsageStats && hasAccessibility && hasNotificationListener
                val priorityMessage = when {
                    !hasOverlay -> "Please grant these permissions to enable app blocking:"
                    !hasUsageStats -> "✅ Overlay granted! Now grant Usage Access:"
                    !hasAccessibility -> "✅ Almost there! Now enable Accessibility Service:"
                    !hasNotificationListener -> "✅ Almost done! Now enable Media Control Access:"
                    else -> "All permissions granted!"
                }

                Text(
                    priorityMessage,
                    color = if (allGranted) Color(0xFF4CAF50) else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = if (allGranted) FontWeight.Bold else FontWeight.Normal
                )

                PermissionItem(
                    title = "1. Display Over Other Apps",
                    description = "Required to show block screen when you open a blocked app",
                    isGranted = hasOverlay,
                    isPriority = !hasOverlay,
                    onGrant = {
                        android.util.Log.d("PermissionsDialog", "User tapped Grant for Overlay")
                        PermissionHelper.requestOverlayPermission(context)
                    }
                )

                PermissionItem(
                    title = "2. Usage Access",
                    description = "Required to detect which app is running and block it",
                    isGranted = hasUsageStats,
                    isPriority = hasOverlay && !hasUsageStats,
                    onGrant = {
                        android.util.Log.d("PermissionsDialog", "User tapped Grant for Usage Stats")
                        PermissionHelper.requestUsageStatsPermission(context)
                    }
                )

                PermissionItem(
                    title = "3. Accessibility Service",
                    description = "Required to fully block apps — stops them from reopening or floating over other apps",
                    isGranted = hasAccessibility,
                    isPriority = hasOverlay && hasUsageStats && !hasAccessibility,
                    onGrant = {
                        android.util.Log.d("PermissionsDialog", "User tapped Grant for Accessibility")
                        showAccessibilityExplainer = true
                    }
                )

                PermissionItem(
                    title = "4. Media Control Access",
                    description = "Required to pause video/audio in blocked apps, not just hide them",
                    isGranted = hasNotificationListener,
                    isPriority = hasOverlay && hasUsageStats && hasAccessibility && !hasNotificationListener,
                    onGrant = {
                        android.util.Log.d("PermissionsDialog", "User tapped Grant for Notification Listener")
                        showNotificationListenerExplainer = true
                    }
                )

                if (!allGranted) {
                    Text(
                        "💡 Tip: After granting permission in Settings, press Back to return here. The dialog will auto-update!",
                        color = Color(0xFF3D8DFF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    hasUsageStats = PermissionHelper.hasUsageStatsPermission(context)
                    hasOverlay = PermissionHelper.hasOverlayPermission(context)
                    hasAccessibility = PermissionHelper.hasAccessibilityPermission(context)
                    hasNotificationListener = PermissionHelper.hasNotificationListenerPermission(context)

                    if (hasUsageStats && hasOverlay && hasAccessibility && hasNotificationListener) {
                        onAllPermissionsGranted()
                    }
                },
                enabled = hasUsageStats && hasOverlay && hasAccessibility && hasNotificationListener,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3D8DFF),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun AccessibilityExplainerDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1C),
        title = {
            Text(
                "Enable Accessibility Service",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "FocusBubble needs this to fully block distracting apps — including stopping them from reopening or floating over other apps.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                ExplainerStep(1, "Tap \"Open Settings\" below")
                ExplainerStep(2, "Find and tap \"FocusBubble\" in the list")
                ExplainerStep(3, "Turn the toggle ON")
                ExplainerStep(4, "Android will show a warning — this is normal for any app blocker. Tap \"Allow\" to continue")
                ExplainerStep(5, "Return to FocusBubble — we'll detect it automatically")
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D8DFF))
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun NotificationListenerExplainerDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1C),
        title = {
            Text(
                "Enable Media Control Access",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "FocusBubble needs this to pause videos or music in blocked apps — so they actually stop, not just hide behind the screen.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                ExplainerStep(1, "Tap \"Open Settings\" below")
                ExplainerStep(2, "Find and tap \"FocusBubble\" in the list")
                ExplainerStep(3, "Turn the toggle ON, then confirm \"Allow\"")
                ExplainerStep(4, "Return to FocusBubble — we'll detect it automatically")
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D8DFF))
            ) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun ExplainerStep(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            "$number.",
            color = Color(0xFF3D8DFF),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(20.dp)
        )
        Text(
            text,
            color = Color.White,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    isPriority: Boolean = false,
    onGrant: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isGranted -> Color(0xFF1E4620)
                isPriority -> Color(0xFF1E3A5F)
                else -> Color(0xFF2C2C2C)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isGranted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF4CAF50) else Color(0xFFFFA726),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(description, color = Color.Gray, fontSize = 12.sp)
                }

                if (!isGranted) {
                    Button(
                        onClick = onGrant,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D8DFF)),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Grant", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}