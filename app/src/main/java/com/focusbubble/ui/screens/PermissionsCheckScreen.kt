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
import com.focusbubble.ui.utils.PermissionHelper

@Composable
fun PermissionsCheckDialog(
    onAllPermissionsGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasUsageStats by remember { mutableStateOf(PermissionHelper.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionHelper.hasOverlayPermission(context)) }

    android.util.Log.d("PermissionsDialog", "Dialog created - Overlay: $hasOverlay, Usage: $hasUsageStats")

    // Continuously recheck permissions while dialog is open
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000) // Check every second
            
            val previousOverlay = hasOverlay
            val previousUsage = hasUsageStats
            
            hasOverlay = PermissionHelper.hasOverlayPermission(context)
            hasUsageStats = PermissionHelper.hasUsageStatsPermission(context)
            
            // Log changes
            if (hasOverlay != previousOverlay) {
                android.util.Log.d("PermissionsDialog", "Overlay changed: $previousOverlay → $hasOverlay")
            }
            if (hasUsageStats != previousUsage) {
                android.util.Log.d("PermissionsDialog", "Usage changed: $previousUsage → $hasUsageStats")
            }
            
            // If both granted, automatically proceed
            if (hasOverlay && hasUsageStats) {
                android.util.Log.d("PermissionsDialog", "✅ Both permissions granted! Auto-proceeding")
                kotlinx.coroutines.delay(500) // Small delay for smooth UX
                onAllPermissionsGranted()
                break
            }
        }
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
                // Show priority message based on what's missing
                val priorityMessage = when {
                    !hasOverlay && !hasUsageStats -> 
                        "Please grant both permissions to enable app blocking:"
                    !hasOverlay -> 
                        "✅ Usage Access granted! Now grant Overlay permission:"
                    !hasUsageStats -> 
                        "✅ Overlay granted! Now grant Usage Access permission:"
                    else -> 
                        "All permissions granted!"
                }
                
                Text(
                    priorityMessage,
                    color = if (hasOverlay && hasUsageStats) Color(0xFF4CAF50) else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = if (hasOverlay && hasUsageStats) FontWeight.Bold else FontWeight.Normal
                )

                // Overlay Permission - Show FIRST (priority)
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

                // Usage Stats Permission - Show SECOND
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

                if (!hasOverlay || !hasUsageStats) {
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
                    // Recheck permissions
                    hasUsageStats = PermissionHelper.hasUsageStatsPermission(context)
                    hasOverlay = PermissionHelper.hasOverlayPermission(context)
                    
                    if (hasUsageStats && hasOverlay) {
                        onAllPermissionsGranted()
                    }
                },
                enabled = hasUsageStats && hasOverlay,
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
                isGranted -> Color(0xFF1E4620)  // Green when granted
                isPriority -> Color(0xFF1E3A5F)  // Blue when it's the priority one
                else -> Color(0xFF2C2C2C)  // Gray when not priority
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                        Text(
                            title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        description,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                if (!isGranted) {
                    Button(
                        onClick = onGrant,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D8DFF)
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Grant", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
