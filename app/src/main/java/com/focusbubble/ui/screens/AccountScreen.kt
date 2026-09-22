package com.focusbubble.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.ui.utils.ProfileImageManager

@Composable
fun AccountScreen(
    userName: String?,
    userEmail: String?,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFavouritesClick: () -> Unit,
    onStarredClick: () -> Unit,
    onAccountClick: () -> Unit,
    onLoggedOut: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val name = userName ?: "User"
    val firstLetter = name.trim().firstOrNull()?.uppercaseChar() ?: 'U'
    val profileBitmap = remember { ProfileImageManager.loadProfileImage(context) }
    val sharedPrefs = remember {
        context.getSharedPreferences("FocusBubblePrefs", android.content.Context.MODE_PRIVATE)
    }
    val aboutText = remember { sharedPrefs.getString("profile_about", "") ?: "" }

    ScreenWithBack(title = "Menu", onBackClick = onBackClick) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Spacer(modifier = Modifier.height(20.dp))

                MenuRow(
                    leading = {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileBitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = profileBitmap.asImageBitmap(),
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(firstLetter.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    title = name,
                    subtitle = aboutText.ifBlank { "Tap to add a status" },
                    onClick = onProfileClick
                )

                Divider(color = Color.White.copy(alpha = 0.08f))

                MenuRow(
                    leading = { Icon(Icons.Default.StarBorder, contentDescription = null, tint = Color.White) },
                    title = "Favourites",
                    subtitle = null,
                    onClick = onFavouritesClick
                )

                Divider(color = Color.White.copy(alpha = 0.08f))

                MenuRow(
                    leading = { Icon(Icons.Default.Star, contentDescription = null, tint = Color.White) },
                    title = "Starred",
                    subtitle = null,
                    onClick = onStarredClick
                )

                Divider(color = Color.White.copy(alpha = 0.08f))

                MenuRow(
                    leading = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White) },
                    title = "Account",
                    subtitle = "Security, notifications",
                    onClick = onAccountClick
                )

                Divider(color = Color.White.copy(alpha = 0.08f))

                Spacer(modifier = Modifier.weight(1f))

                androidx.compose.material3.Button(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                        .height(50.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color(0xFFFF6B6B)
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)
                ) {
                    Text("Log out", fontWeight = FontWeight.SemiBold)
                }
            }

            if (showLogoutConfirm) {
                AlertDialog(
                    onDismissRequest = { showLogoutConfirm = false },
                    title = { Text("Log out?") },
                    text = { Text("You'll need to sign in again to use FocusBubble.") },
                    confirmButton = {
                        TextButton(onClick = {
                            com.focusbubble.ui.utils.UserSession.clearUser(context)
                            showLogoutConfirm = false
                            onLoggedOut()
                        }) { Text("Log out", color = Color(0xFFFF6B6B)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.4f)
        )
    }
}
