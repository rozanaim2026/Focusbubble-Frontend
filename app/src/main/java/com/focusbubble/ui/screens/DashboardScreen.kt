package com.focusbubble.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.focusbubble.R
import com.focusbubble.ui.components.TopBar
import com.focusbubble.ui.components.BottomNavBar
import com.focusbubble.ui.components.QuoteMascot
import com.focusbubble.ui.sheets.EditOptionsSheet
import com.focusbubble.ui.viewmodel.BlockedAppsViewModel
import com.focusbubble.ui.viewmodel.FocusStatsViewModel
import com.focusbubble.ui.utils.PermissionHelper

@Composable
fun DashboardScreen(
    userName: String?,
    navController: NavHostController,
    onMenuClick: () -> Unit = {},
    onStartFocusClick: (sessionMinutes: Int) -> Unit = {},
    onSchedulesClick: () -> Unit = {},
    onChatsClick: () -> Unit = {},
    onBlocksClick: () -> Unit = {},
    onQuotesClick: () -> Unit = {}   // ✅ add this new line
)

{

    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Dashboard") }
    var showEditOptionsSheet by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.d("DashboardScreen", "Notification permission: $isGranted")
    }

    val blockedAppsViewModel: BlockedAppsViewModel = hiltViewModel()
    // Scoped to the Activity — matching AppNavHost's focusStatsViewModel in
    // MainActivity.kt exactly — so both refer to the SAME instance/StateFlow.
    // Previously this called plain hiltViewModel() with no explicit owner,
    // which resolves to a ViewModelStore scoped to THIS screen's own
    // NavBackStackEntry — a completely separate instance from the one the
    // app-wide session-finished broadcast receiver (in AppNavHost) calls
    // .refresh() on. That mismatch is why the weekly timer never updated
    // live: the receiver really was refreshing a FocusStatsViewModel, just
    // not the one this screen's collectAsState() below is actually watching.
    val focusStatsViewModel: FocusStatsViewModel =
        hiltViewModel(context as androidx.activity.ComponentActivity)

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.dashboard_bg),
            contentDescription = "Dashboard Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            TopBar(userName = userName ?: "User", onMenuClick = onMenuClick)
            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ProfileFocusCard(
                    userName = userName,
                    focusStatsViewModel = focusStatsViewModel,
                    blockedAppsViewModel = blockedAppsViewModel,
                    onEditClick = { showEditOptionsSheet = true }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuoteMascot(
                    size = 96.dp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Start Focus Session Button - with step-by-step permission check
                Button(
                    onClick = {
                        // Step-by-step permission flow:
                        // 1️⃣ Check notification permission FIRST (Android 13+)
                        val hasNotification = PermissionHelper.hasNotificationPermission(context)
                        val hasOverlay = PermissionHelper.hasOverlayPermission(context)
                        val hasUsageStats = PermissionHelper.hasUsageStatsPermission(context)
                        val hasAccessibility = PermissionHelper.hasAccessibilityPermission(context)
                        val hasNotificationListener = PermissionHelper.hasNotificationListenerPermission(context)
                        android.util.Log.d("DashboardScreen", "Permission check - Notification: $hasNotification, Overlay: $hasOverlay, Usage: $hasUsageStats")

                        when {
                            // If notification not granted (Android 13+), ask for it FIRST
                            !hasNotification -> {
                                android.util.Log.d("DashboardScreen", "Notification permission missing, requesting")
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            // If overlay not granted, ask for it
                            !hasOverlay -> {
                                android.util.Log.d("DashboardScreen", "Overlay permission missing, showing dialog")
                                showPermissionsDialog = true
                            }
                            // If overlay granted but usage not granted, ask for usage
                            !hasUsageStats -> {
                                android.util.Log.d("DashboardScreen", "Usage permission missing, showing dialog")
                                showPermissionsDialog = true
                            }

                            // If usage granted but accessibility not granted, ask for it
                            !hasAccessibility -> {
                                android.util.Log.d("DashboardScreen", "Accessibility permission missing, showing dialog")
                                showPermissionsDialog = true
                            }
                            // If all granted, start session!
                            // If all granted, start session!
                            else -> {
                                val blockedCount = blockedAppsViewModel.blockedApps.value.size
                                if (blockedCount == 0) {
                                    android.util.Log.d("DashboardScreen", "No apps selected — prompting user to pick before starting")
                                    android.widget.Toast.makeText(
                                        context,
                                        "Select at least one app to block before starting a session",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    showEditOptionsSheet = true
                                } else {
                                    android.util.Log.d("DashboardScreen", "All permissions granted, starting session")
                                    val duration = blockedAppsViewModel.selectedDurationMinutes.value
                                    navController.navigate("focusSession/$duration")
                                }
                            }
                        }
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
                        text = "Start Focus Session",
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (showEditOptionsSheet) {
            EditOptionsSheet(
                viewModel = blockedAppsViewModel,
                onDismiss = { showEditOptionsSheet = false }
            )
        }

        if (showPermissionsDialog) {
            PermissionsCheckDialog(
                onAllPermissionsGranted = {
                    showPermissionsDialog = false
                    val blockedCount = blockedAppsViewModel.blockedApps.value.size
                    if (blockedCount == 0) {
                        android.widget.Toast.makeText(
                            context,
                            "Select at least one app to block before starting a session",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        showEditOptionsSheet = true
                    } else {
                        val duration = blockedAppsViewModel.selectedDurationMinutes.value
                        navController.navigate("focusSession/$duration")
                    }
                },
                onDismiss = { showPermissionsDialog = false }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        "Schedules" -> onSchedulesClick()
                        "Chats" -> onChatsClick()
                        "Blocks" -> onBlocksClick()
                        "Quotes" -> onQuotesClick()
                    }
                }
            )
        }
    }
}