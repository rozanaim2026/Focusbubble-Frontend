package com.focusbubble.ui.sheets

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.util.Log
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.draw.scale

/**
 * Process-lifetime cache of the installed-app scan. Scanning ~250 apps and
 * converting each icon to a bitmap is what was causing the multi-second delay
 * opening this screen — caching means every open after the first is instant.
 */
private object InstalledAppsCache {
    var apps: List<UserAppInfo>? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockAppsSheet(
    viewModel: BlockedAppsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val blockedApps by viewModel.blockedApps.collectAsState()

    var allApps by remember { mutableStateOf(InstalledAppsCache.apps ?: emptyList()) }
    var isLoading by remember { mutableStateOf(InstalledAppsCache.apps == null) }
    var selectedPackages by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionAsked by remember { mutableStateOf(false) }

    LaunchedEffect(blockedApps) {
        selectedPackages = blockedApps.map { it.packageName }.toSet()
    }

    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Load off the main thread — this is what was causing the ~5s freeze on open.
    // Everything here (search bar, title, Confirm button) still renders immediately;
    // only the list itself waits on this.
    LaunchedEffect(Unit) {
        if (InstalledAppsCache.apps != null) return@LaunchedEffect // already cached

        val apps = withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            installedApps
                .filter { appInfo ->
                    val pkg = appInfo.packageName
                    val isUserInstalled = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                    val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    val hasLauncherIntent = try {
                        pm.getLaunchIntentForPackage(pkg) != null
                    } catch (e: Exception) {
                        false
                    }
                    val isOwnApp = pkg == context.packageName
                    (isUserInstalled || isUpdatedSystemApp) && hasLauncherIntent && !isOwnApp
                }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }
                .mapNotNull { appInfo ->
                    try {
                        val label = appInfo.loadLabel(pm).toString()
                        val drawable = pm.getApplicationIcon(appInfo.packageName)
                        UserAppInfo(
                            appName = label,
                            packageName = appInfo.packageName,
                            drawable = drawable,
                            iconBitmap = drawableToImageBitmap(drawable)
                        )
                    } catch (e: Exception) {
                        Log.w("BlockAppsSheet", "Failed to load app: ${appInfo.packageName}", e)
                        null
                    }
                }
        }

        InstalledAppsCache.apps = apps
        allApps = apps
        isLoading = false
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Select Apps to Block", fontSize = 18.sp, color = Color.White)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search apps...", color = Color.Gray,fontSize = 13.sp) },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF3D8DFF),
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            Text(
                if (isLoading) "Loading apps..." else "${filteredApps.size} apps found",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))

            // weight(1f) makes this take exactly the remaining space — no arbitrary
            // fixed heights, no empty gap above the Confirm button, and it correctly
            // adapts to any screen size.
            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3D8DFF))
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            val isSelected = selectedPackages.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFF2C2C2C),
                                        androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                    )
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                app.iconBitmap?.let { bitmap ->
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = app.appName,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(app.appName, color = Color.White, modifier = Modifier.weight(1f))

                                Switch(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked && !Settings.canDrawOverlays(context)) {
                                            if (!permissionAsked) {
                                                showPermissionDialog = true
                                                permissionAsked = true
                                            } else {
                                                val intent = Intent(
                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:${context.packageName}")
                                                )
                                                context.startActivity(intent)
                                            }
                                        } else {
                                            selectedPackages = if (checked) {
                                                selectedPackages + app.packageName
                                            } else {
                                                selectedPackages - app.packageName
                                            }
                                        }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF3D8DFF),
                                        checkedTrackColor = Color(0xFF0D47A1)
                                    ),
                                            modifier = Modifier.scale(0.85f)

                                )
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.updateBlockedApps(selectedPackages, allApps)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .navigationBarsPadding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            ) {
                Text("Confirm (${selectedPackages.size} selected)", fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required", color = Color.White) },
            text = {
                Text(
                    "FocusBubble needs permission to display over other apps to block distractions during your focus sessions.\n\nThis is essential for the app blocking feature to work.",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                ) {
                    Text("Grant Permission", color = Color(0xFF3D8DFF))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF2C2C2C)
        )
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
