package com.focusbubble.ui.sheets

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
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

    // Load previously blocked apps from database
    val blockedApps by viewModel.blockedApps.collectAsState()
    
    var allApps by remember { mutableStateOf<List<UserAppInfo>>(emptyList()) }
    var selectedPackages by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionAsked by remember { mutableStateOf(false) }
    
    // Initialize selectedPackages with previously blocked apps
    LaunchedEffect(blockedApps) {
        val previouslySelected = blockedApps.map { it.packageName }.toSet()
        selectedPackages = previouslySelected
        Log.d("BlockAppsSheet", "Initialized with ${previouslySelected.size} previously blocked apps: $previouslySelected")
    }
    
    // Filter apps based on search query
    val filteredApps = remember(allApps, searchQuery) {
        if (searchQuery.isBlank()) {
            allApps
        } else {
            allApps.filter { it.appName.contains(searchQuery, ignoreCase = true) }
        }
    }

    LaunchedEffect(Unit) {
        val apps = mutableListOf<UserAppInfo>()
        val pm = context.packageManager

        // Get ALL installed applications with QUERY_ALL_PACKAGES permission
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        Log.d("BlockAppsSheet", "Total installed apps: ${installedApps.size}")
        
        installedApps
            .filter { appInfo ->
                val pkg = appInfo.packageName
                
                // ONLY show user-installed apps (NOT system apps)
                // System apps have FLAG_SYSTEM flag set
                val isUserInstalled = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                
                // Also check for updated system apps (like pre-installed YouTube, Instagram on some phones)
                val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                
                // Check if app has a launcher icon (can be launched by user)
                val hasLauncherIntent = try {
                    pm.getLaunchIntentForPackage(pkg) != null
                } catch (e: Exception) {
                    false
                }
                
                // Exclude our own app
                val isOwnApp = pkg == context.packageName
                
                // Include if:
                // 1. User-installed app OR updated system app (like pre-installed social media)
                // 2. Has launcher icon (can be opened by user)
                // 3. Not our own app
                (isUserInstalled || isUpdatedSystemApp) && hasLauncherIntent && !isOwnApp
            }
            .sortedBy { it.loadLabel(pm).toString().lowercase() }
            .forEach { appInfo ->
                try {
                    val label = appInfo.loadLabel(pm).toString()
                    val drawable = pm.getApplicationIcon(appInfo.packageName)
                    
                    apps.add(
                        UserAppInfo(
                            appName = label,
                            packageName = appInfo.packageName,
                            drawable = drawable,
                            iconBitmap = drawableToImageBitmap(drawable)
                        )
                    )
                    Log.d("BlockAppsSheet", "Added app: $label (${appInfo.packageName})")
                } catch (e: Exception) {
                    Log.w("BlockAppsSheet", "Failed to load app: ${appInfo.packageName}", e)
                }
            }

        allApps = apps
        Log.d("BlockAppsSheet", "Loaded ${apps.size} apps to show")
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
                
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search apps...", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3D8DFF),
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                
                // Show app count
                Text(
                    "${filteredApps.size} apps found",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))

                // Scrollable list
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredApps) { app ->
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
                                    // If trying to select an app and permission not granted
                                    if (checked && !Settings.canDrawOverlays(context)) {
                                        // Show dialog only once (first time)
                                        if (!permissionAsked) {
                                            showPermissionDialog = true
                                            permissionAsked = true
                                        } else {
                                            // Already asked once, just open settings
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        // Permission granted or deselecting app
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
    
    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { 
                Text(
                    "Permission Required",
                    color = Color.White
                ) 
            },
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
                        // Open settings to grant permission
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
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
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
