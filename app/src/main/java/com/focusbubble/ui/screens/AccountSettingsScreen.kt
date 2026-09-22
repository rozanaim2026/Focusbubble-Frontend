package com.focusbubble.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import android.util.Log
import android.content.Context
import android.content.Intent
import android.provider.Settings
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.ui.utils.UserSession


@Composable
fun AccountSettingsScreen(
    onBackClick: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    val deleteScope = rememberCoroutineScope()

    ScreenWithBack(title = "Account", onBackClick = onBackClick) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Security", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Manage how you sign in", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notifications", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Session and reminder alerts", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color.White
                        )
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                // Accessibility permission
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Accessibility Permission",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Required so FocusBubble can fully block distracting apps, even Picture-in-Picture windows.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = "Open Accessibility Settings",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF6B6B))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Delete Account", color = Color(0xFFFF6B6B), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Permanently deletes your account and all data", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B6B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Delete Account", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete account?") },
                    text = {
                        Text(
                            "This permanently deletes your account and all associated " +
                                    "data — session history, weekly focus time, blocked apps, and " +
                                    "preferences. This cannot be undone."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = !isDeleting,
                            onClick = {
                                isDeleting = true
                                deleteScope.launch {
                                    try {
                                        val userId = UserSession.getUserId(context)
                                        if (userId != -1) {
                                            // Backend first — if this fails, we deliberately
                                            // do NOT wipe local data, so the user isn't left
                                            // signed-out with no account anywhere.
                                            com.focusbubble.data.network.RetrofitClient.api.deleteUser(userId)

                                            com.focusbubble.data.AppDatabase.getInstance(context).let { db ->
                                                db.focusSessionDao() // sessions cascade via FK on user delete
                                                db.userDao().deleteUser(userId)
                                                db.blockedAppDao().clearAllForUser(userId)
                                            }
                                            com.focusbubble.ui.utils.QuotePreferences.clearForUser(context, userId)
                                            com.focusbubble.service.WeeklyStatsManager.clearForUser(context, userId)
                                            context.getSharedPreferences("FocusBubblePrefs", Context.MODE_PRIVATE)
                                                .edit()
                                                .remove("selected_duration_$userId")
                                                .apply()
                                            val isGuest = UserSession.getUserEmail(context)?.contains("guest_") == true
                                            UserSession.clearUser(context)
                                            if (isGuest) UserSession.clearGuestEmail(context)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AccountSettings", "Failed to delete account", e)
                                    } finally {
                                        isDeleting = false
                                        showDeleteConfirm = false
                                        onLoggedOut()
                                    }
                                }
                            }
                        ) { Text(if (isDeleting) "Deleting..." else "Delete", color = Color(0xFFFF6B6B)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}