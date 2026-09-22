package com.focusbubble

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.focusbubble.service.BlockerService
import com.focusbubble.ui.screens.*
import com.focusbubble.ui.theme.FocusBubbleTheme
import com.focusbubble.ui.viewmodel.BlockedAppsViewModel
import com.focusbubble.ui.viewmodel.FocusStatsViewModel
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val REQUEST_NOTIFICATION_PERMISSION = 1001
    private var startServiceAfterPermission = false
    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw fully edge-to-edge — otherwise the default theme colors (Material
        // purple status bar, light nav bar) show through above/below our content.
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Initialize ThreeTen
        AndroidThreeTen.init(this)

        // Google Sign-In setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("464315770315-n5ca91qnc38nmj8039put1ggv22c10n7.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set Compose content
        setContent {
            FocusBubbleTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val sharedPrefs = getSharedPreferences("FocusBubblePrefs", Context.MODE_PRIVATE)
                    AppNavHost(
                        sharedPrefs = sharedPrefs,
                        onStartFocusClick = { duration -> startFocusServiceIfPermissionGranted(duration) }
                    )
                }
            }
        }
    }

    private fun startFocusServiceIfPermissionGranted(durationMinutes: Int = 25) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                startServiceAfterPermission = true
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            } else {
                startFocusService(durationMinutes)
            }
        } else {
            startFocusService(durationMinutes)
        }
    }

    private fun startFocusService(durationMinutes: Int = 25) {
        val serviceIntent = Intent(this, BlockerService::class.java).apply {
            putExtra("DURATION_MINUTES", durationMinutes)
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (startServiceAfterPermission) {
                    startFocusService()
                    startServiceAfterPermission = false
                }
            } else {
                Toast.makeText(
                    this,
                    "Notification permission is required to start focus session",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

@Composable
fun AppNavHost(
    sharedPrefs: android.content.SharedPreferences,
    onStartFocusClick: (Int) -> Unit
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val blockedAppsViewModel: BlockedAppsViewModel = hiltViewModel()
    // Explicitly scoped to the Activity (not the default "current nav
    // destination" scope) so this is the SAME instance every screen gets when
    // it calls hiltViewModel() the same way — see the matching change in
    // DashboardScreen.kt. Without this, Dashboard's hiltViewModel() call
    // (made inside its own composable("dashboard") { ... } route) resolves to
    // a ViewModelStore scoped to that NavBackStackEntry, while this call here
    // (made outside any route, directly in AppNavHost) resolves to the
    // Activity's ViewModelStore instead. Those are two DIFFERENT ViewModel
    // instances with two separate StateFlow objects — calling refresh() below
    // was updating one instance while Dashboard's UI kept observing the
    // other, completely untouched, one. That's why the weekly timer only
    // ever reflected reality after a full relaunch (fresh instance, fresh
    // read) and never updated live.
    val focusStatsViewModel: FocusStatsViewModel = hiltViewModel(context as androidx.activity.ComponentActivity)

    // Fires the INSTANT a session finishes naturally, regardless of which screen
    // the user is on (Dashboard, Blocks, Schedules, even FocusSessionScreen
    // itself) and without requiring the app to be backgrounded first.
    //
    // BlockerService.handleSessionFinished() already sends this broadcast the
    // moment a session completes (see BlockerService.kt line ~306) — but until
    // now nothing in the app was listening for it. The only two places that ever
    // checked for a finished session were determineStartDestination() (only runs
    // on a fresh process launch) and Dashboard's own ON_RESUME observer (only
    // fires on an actual background->foreground transition). If the user stayed
    // inside the app the whole session — the normal case — neither of those ever
    // ran, so the weekly stats stayed stale and the congratulations screen never
    // appeared until the app was force-quit and reopened. Listening for the
    // broadcast directly closes that gap: the stats refresh and the celebration
    // screen now both fire live, no matter what the user is doing in the app.
    androidx.compose.runtime.DisposableEffect(Unit) {
        val sessionFinishedReceiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                android.util.Log.d("SessionFinishedReceiver", "📡 Broadcast received")
                focusStatsViewModel.refresh()

                // If FocusSessionScreen is currently the mounted screen, IT owns
                // this transition entirely (see handleSessionEndedIfNeeded() in
                // FocusSessionScreen.kt) — step aside completely rather than also
                // acting on the same broadcast. Previously both this receiver and
                // FocusSessionScreen's own listener reacted independently to the
                // same natural-finish signal, each unaware of the other:
                // whichever one cleared hasPendingCelebration() first "won" and
                // navigated to the celebration screen, while the loser then found
                // the flag already false and fell back to onStop() — silently
                // popping back to Dashboard right on top of the celebration
                // screen the winner had just shown. That race is exactly why the
                // congratulations screen kept disappearing even though the
                // completion itself was being recorded correctly every time.
                val currentRoute = navController.currentDestination?.route
                if (currentRoute?.startsWith("focusSession") == true) {
                    android.util.Log.d("SessionFinishedReceiver", "FocusSessionScreen is active — deferring to it, not touching celebration state")
                    return
                }

                val hasCelebration = com.focusbubble.service.SessionStateManager.hasPendingCelebration(context)
                android.util.Log.d("SessionFinishedReceiver", "hasPendingCelebration=$hasCelebration")
                if (hasCelebration) {
                    val seconds = com.focusbubble.service.SessionStateManager.getPendingCelebrationSeconds(context)
                    com.focusbubble.service.SessionStateManager.clearPendingCelebration(context)
                    android.util.Log.d("SessionFinishedReceiver", "seconds=$seconds currentRoute=$currentRoute")
                    // Avoid double-navigating if we're already showing the
                    // celebration screen for some other reason.
                    if (currentRoute?.startsWith("sessionComplete") != true) {
                        android.util.Log.d("SessionFinishedReceiver", "Navigating to sessionComplete/$seconds")
                        navController.navigate("sessionComplete/$seconds") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    }
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            sessionFinishedReceiver,
            IntentFilter(com.focusbubble.service.SessionStateManager.ACTION_SESSION_FINISHED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            try {
                context.unregisterReceiver(sessionFinishedReceiver)
            } catch (e: IllegalArgumentException) {
                // Already unregistered — safe to ignore.
            }
        }
    }

    NavHost(navController = navController, startDestination = determineStartDestination(context)) {

        composable("welcome") {
            WelcomeScreen(onContinue = { name ->
                val userId = com.focusbubble.ui.utils.UserSession.getUserId(context)
                if (userId != -1 && name.isNotBlank()) {
                    val isGuest = com.focusbubble.ui.utils.UserSession.getUserEmail(context)
                        ?.endsWith("@focusbubble.app") == true
                    val nameKey = "profile_name_$userId"
                    val alreadySaved = sharedPrefs.contains(nameKey)
                    // Google users: always take the fresh name from their
                    // account (it might have changed on Google's side).
                    // Guests: only set it the FIRST time this user id is seen
                    // — a RETURNING guest who already customized their name in
                    // Profile must keep it, not have it silently stomped back
                    // to the generic "Guest" default on every Skip tap.
                    if (!isGuest || !alreadySaved) {
                        sharedPrefs.edit().putString(nameKey, name).apply()
                    }
                }
                navController.navigate("intro")
            })
        }

        composable("intro") {
            BubblyIntroScreen(onContinue = {
                navController.navigate("dashboard") {
                    popUpTo("welcome") { inclusive = true }
                }
            })
        }

        composable("dashboard") {
            val profileName = sharedPrefs.getString("profile_name_${com.focusbubble.ui.utils.UserSession.getUserId(context)}", "User") ?: "User"
            val context = androidx.compose.ui.platform.LocalContext.current
            // Fires every time navigation actually LANDS on dashboard — after
            // fresh login, after guest Skip, and after logout->relogin — not
            // just on ON_RESUME (which doesn't fire for pure in-app
            // navigation, same limitation that caused the earlier
            // congratulations-screen bug). This is what makes blockedApps,
            // selected duration, and weekly stats correctly re-point at
            // whichever user just landed here, instead of showing stale data
            // left over from whoever was logged in before.
            androidx.compose.runtime.LaunchedEffect(Unit) {
                blockedAppsViewModel.refreshForCurrentUser()
                focusStatsViewModel.refresh()
            }
            // Catch the case where a session finished naturally while the user was
            // elsewhere in the app (not closed, just on a different screen) — the
            // app-relaunch case is handled by determineStartDestination below.
            //
            // Uses ON_RESUME (not LaunchedEffect(Unit)) deliberately: LaunchedEffect(Unit)
            // only runs once for the lifetime of this composable's composition, so if
            // Dashboard's composition is retained on the back stack (rather than freshly
            // recomposed) when navigating back to it after a session, the check would
            // silently never re-run — which is exactly why the congratulations screen
            // was going missing even though the completion was being saved correctly.
            val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
            androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        focusStatsViewModel.refresh()
                        if (com.focusbubble.service.SessionStateManager.hasPendingCelebration(context)) {
                            val seconds = com.focusbubble.service.SessionStateManager.getPendingCelebrationSeconds(context)
                            com.focusbubble.service.SessionStateManager.clearPendingCelebration(context)
                            navController.navigate("sessionComplete/$seconds")
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            DashboardScreen(
                userName = profileName,
                navController = navController,
                onMenuClick = { navController.navigate("account") },
                onStartFocusClick = { duration ->
                    navController.navigate("focusSession/$duration")
                },
                onSchedulesClick = { navController.navigate("schedules") },
                onChatsClick = { navController.navigate("chats") },
                onBlocksClick = { navController.navigate("blocks") },
                onQuotesClick = { navController.navigate("quotes") }
            )
        }

        composable("account") {
            val profileName = sharedPrefs.getString("profile_name_${com.focusbubble.ui.utils.UserSession.getUserId(context)}", "User") ?: "User"
            val context = androidx.compose.ui.platform.LocalContext.current
            val userEmail = remember { com.focusbubble.ui.utils.UserSession.getUserEmail(context) }

            AccountScreen(
                userName = profileName,
                userEmail = userEmail,
                onBackClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate("profileDetail") },
                onFavouritesClick = { navController.navigate("favourites") },
                onStarredClick = { navController.navigate("starred") },
                onAccountClick = { navController.navigate("accountSettings") },
                onLoggedOut = {
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("profileDetail") {
            val profileName = sharedPrefs.getString("profile_name_${com.focusbubble.ui.utils.UserSession.getUserId(context)}", "User") ?: "User"
            val context = androidx.compose.ui.platform.LocalContext.current
            val userEmail = remember { com.focusbubble.ui.utils.UserSession.getUserEmail(context) }

            ProfileDetailScreen(
                userName = profileName,
                userEmail = userEmail,
                onBackClick = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("accountSettings") {
            AccountSettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("favourites") {
            ComingSoonScreen(title = "Favourites", onBackClick = { navController.popBackStack() })
        }

        composable("starred") {
            ComingSoonScreen(title = "Starred", onBackClick = { navController.popBackStack() })
        }

        composable("schedules") {
            SchedulesScreen(onBackClick = { navController.popBackStack() })
        }

        composable("chats") {
            ChatsScreen(onBackClick = { navController.popBackStack() })
        }

        composable("blocks") {
            BlocksScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = blockedAppsViewModel
            )
        }

        composable("quotes") {
            QuotesScreen(onBackClick = { navController.popBackStack() })
        }

        composable("focusSession/{duration}") { backStackEntry ->
            val duration = backStackEntry.arguments?.getString("duration")?.toInt() ?: 25
            FocusSessionScreen(
                durationMinutes = duration,
                focusStatsViewModel = focusStatsViewModel,
                onStop = { navController.popBackStack() },
                onSessionComplete = { seconds ->
                    // Replaces the focusSession destination with sessionComplete
                    // directly (rather than popping back to dashboard first and
                    // hoping a separate receiver navigates forward afterward) —
                    // this is the actual fix for the congratulations screen not
                    // appearing. See the long comment on FocusSessionScreen's
                    // onSessionComplete parameter for the full race-condition
                    // explanation.
                    navController.navigate("sessionComplete/$seconds") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                },
                onFocusSessionsClick = {
                    navController.navigate("dashboard")
                }
            )
        }

        composable("sessionComplete/{duration}") { backStackEntry ->
            val seconds = backStackEntry.arguments?.getString("duration")?.toInt() ?: 60
            SessionCompleteScreen(
                durationSeconds = seconds,
                onBackToHome = {
                    // Might be the very first screen (app reopened straight into
                    // celebration) or reached from a live session — either way,
                    // land cleanly on dashboard with nothing stale above it.
                    val popped = navController.popBackStack("dashboard", inclusive = false)
                    if (!popped) {
                        navController.navigate("dashboard") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Decides which screen to land on when the app (re)opens — including after the
 * whole process was killed and relaunched, e.g. from tapping the floating timer
 * while the app was backgrounded. Previously this always started at "welcome",
 * which silently logged the user out and abandoned any in-progress session.
 */
private fun determineStartDestination(context: android.content.Context): String {
    val isLoggedIn = com.focusbubble.ui.utils.UserSession.getUserId(context) != -1
    if (!isLoggedIn) return "welcome"

    // A session finished while the app was fully closed — show the celebration
    // as soon as the app reopens, exactly once (cleared once actually shown).
    if (com.focusbubble.service.SessionStateManager.hasPendingCelebration(context)) {
        val seconds = com.focusbubble.service.SessionStateManager.getPendingCelebrationSeconds(context)
        com.focusbubble.service.SessionStateManager.clearPendingCelebration(context)
        return "sessionComplete/$seconds"
    }

    val hasActiveSession = com.focusbubble.service.SessionStateManager.isSessionActive(context)
    if (hasActiveSession) {
        val totalMinutes = (com.focusbubble.service.SessionStateManager.getTotalDuration(context) / 60000L)
            .toInt()
            .coerceAtLeast(1)
        return "focusSession/$totalMinutes"
    }

    return "dashboard"
}