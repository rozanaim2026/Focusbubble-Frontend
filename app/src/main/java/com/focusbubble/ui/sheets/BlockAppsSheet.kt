package com.focusbubble.ui.sheets

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusbubble.ui.utils.UserAppInfo
import com.focusbubble.ui.viewmodel.BlockedAppsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockAppsSheet(
    viewModel: BlockedAppsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    var allApps by remember { mutableStateOf<List<UserAppInfo>>(emptyList()) }
    var selectedPackages by remember { mutableStateOf(viewModel.blockedApps.value.map { it.packageName }.toSet()) }

    LaunchedEffect(Unit) {
        // Define popular distracting apps
        val popularDistractingApps = listOf(
            "com.instagram.android", // Instagram
            "com.snapchat.android",  // Snapchat
            "com.facebook.katana",   // Facebook
            "com.facebook.orca",     // Messenger
            "com.twitter.android",   // Twitter/X
            "com.youtube.android",   // YouTube
            "com.tiktok.android",    // TikTok
            "com.reddit.frontpage",  // Reddit
            "com.pinterest",         // Pinterest
            "com.netflix.mediaclient", // Netflix
            "com.spotify.music",     // Spotify
            "com.discord",           // Discord
            "com.whatsapp",          // WhatsApp
            "com.telegram.ui",       // Telegram
        )

        val apps = mutableListOf<UserAppInfo>()
        val pm = context.packageManager

        // Get only installed popular distracting apps
        popularDistractingApps.forEach { packageName ->
            try {
                val appInfo = pm.getApplicationInfo(packageName, 0)
                val drawable = pm.getApplicationIcon(packageName)
                apps.add(
                    UserAppInfo(
                        appName = appInfo.loadLabel(pm).toString(),
                        packageName = packageName,
                        drawable = drawable,
                        iconBitmap = drawableToImageBitmap(drawable)
                    )
                )
            } catch (e: PackageManager.NameNotFoundException) {
                // App not installed, skip it
            }
        }

        allApps = apps
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C1C1C),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                // FIXED BUTTON AT BOTTOM - Always visible
                Button(
                    onClick = {
                        viewModel.updateBlockedApps(selectedPackages, allApps)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                ) {
                    Text("Confirm (${selectedPackages.size} selected)", fontSize = 16.sp)
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text("Select Apps to Block", fontSize = 20.sp, color = Color.White)
                Spacer(Modifier.height(12.dp))

                // Scrollable list
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(allApps) { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF2C2C2C),
                                    androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            app.iconBitmap?.let { bitmap ->
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = app.appName,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(app.appName, color = Color.White, modifier = Modifier.weight(1f))

                            Switch(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (!Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        selectedPackages = if (checked) {
                                            selectedPackages + app.packageName
                                        } else {
                                            selectedPackages - app.packageName
                                        }
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF3D8DFF))
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// Helper: Drawable → ImageBitmap
private fun drawableToImageBitmap(drawable: Drawable?): ImageBitmap? {
    drawable ?: return null
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
        drawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap.asImageBitmap()
}
