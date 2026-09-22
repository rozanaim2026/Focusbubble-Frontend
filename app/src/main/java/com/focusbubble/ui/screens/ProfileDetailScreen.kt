package com.focusbubble.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.ui.utils.ProfileImageManager
import com.focusbubble.ui.utils.UserSession

@Composable
fun ProfileDetailScreen(
    userName: String?,
    userEmail: String?,
    onBackClick: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("FocusBubblePrefs", android.content.Context.MODE_PRIVATE)
    }

    val isGuestAccount = userEmail?.endsWith("@focusbubble.app") == true

    var name by remember { mutableStateOf(userName ?: "User") }
    val currentUserId = remember { UserSession.getUserId(context) }
    var about by remember { mutableStateOf(sharedPrefs.getString("profile_about_$currentUserId", "") ?: "") }
    var phone by remember { mutableStateOf(sharedPrefs.getString("profile_phone_$currentUserId", "") ?: "") }
    var editingName by remember { mutableStateOf(false) }
    var editingAbout by remember { mutableStateOf(false) }
    var editingPhone by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showPhotoViewer by remember { mutableStateOf(false) }

    val firstLetter = name.trim().firstOrNull()?.uppercaseChar() ?: 'U'
    var profileBitmap by remember { mutableStateOf<Bitmap?>(ProfileImageManager.loadProfileImage(context)) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (ProfileImageManager.saveProfileImage(context, it)) {
                profileBitmap = ProfileImageManager.loadProfileImage(context)
            }
        }
    }

    ScreenWithBack(title = "Profile", onBackClick = onBackClick) { modifier ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile circle
                Box(modifier = Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        Color.White.copy(alpha = 0.25f),
                                        Color.White.copy(alpha = 0.9f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { showProfileMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap!!.asImageBitmap(),
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(firstLetter.toString(), color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color.Black, CircleShape)
                            .clickable { showProfileMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Change photo", tint = Color.Black, modifier = Modifier.size(14.dp))
                    }

                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false },
                        modifier = Modifier.background(Color(0xFF1C1C1C))
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Profile", color = Color.White) },
                            onClick = {
                                showProfileMenu = false
                                if (profileBitmap != null) showPhotoViewer = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Profile", color = Color.White) },
                            onClick = {
                                showProfileMenu = false
                                imagePickerLauncher.launch("image/*")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // About
                // About
                ProfileField(
                    label = "About",
                    value = about,
                    isEditing = editingAbout,
                    editable = true,
                    placeholder = "Add a status",
                    onEditToggle = { editingAbout = !editingAbout },
                    onValueChange = { about = it },
                    onSave = {
                        sharedPrefs.edit().putString("profile_about_$currentUserId", about).apply()
                        editingAbout = false
                    }
                )

// Name
                ProfileField(
                    label = "Name",
                    value = name,
                    isEditing = editingName,
                    editable = isGuestAccount,
                    placeholder = "Your name",
                    onEditToggle = { if (isGuestAccount) editingName = !editingName },
                    onValueChange = { name = it },
                    onSave = {
                        sharedPrefs.edit().putString("profile_name_$currentUserId", name).apply()
                        editingName = false
                    }
                )

                // Email — always read-only
                ProfileField(
                    label = "Email",
                    value = userEmail ?: "—",
                    isEditing = false,
                    editable = false,
                    placeholder = "",
                    onEditToggle = {},
                    onValueChange = {},
                    onSave = {}
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = Color(0xFFFF6B6B)
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
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
                            // Logout must NEVER delete data — only end the
                            // session. This used to wipe profile_name/about/
                            // phone on every logout, directly contradicting
                            // that. Removed entirely; nothing here needs
                            // clearing on logout.
                            UserSession.clearUser(context)
                            showLogoutConfirm = false
                            onLoggedOut()
                        }) { Text("Log out", color = Color(0xFFFF6B6B)) } },
                    dismissButton = {
                        TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
                    }
                )
            }

            if (showPhotoViewer && profileBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { showPhotoViewer = false },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = profileBitmap!!.asImageBitmap(),
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
                    IconButton(
                        onClick = { showPhotoViewer = false },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    editable: Boolean,
    placeholder: String,
    onEditToggle: () -> Unit,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = label != "About",
                    placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.3f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
                TextButton(onClick = onSave) { Text("Save") }
            } else {
                Text(
                    text = value.ifBlank { placeholder },
                    color = if (value.isBlank()) Color.White.copy(alpha = 0.35f) else Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (editable) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit $label",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp).clickable { onEditToggle() }
                    )
                }
            }
        }
    }
}
