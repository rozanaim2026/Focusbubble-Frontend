package com.focusbubble

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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

        // Initialize ThreeTen
        AndroidThreeTen.init(this)

        // Google Sign-In setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
    val blockedAppsViewModel: BlockedAppsViewModel = hiltViewModel()
    val focusStatsViewModel: FocusStatsViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = "welcome") {

        composable("welcome") {
            WelcomeScreen(onContinue = { name ->
                if (name.isNotBlank()) {
                    sharedPrefs.edit().putString("profile_name", name).apply()
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
            val profileName = sharedPrefs.getString("profile_name", "User") ?: "User"

            DashboardScreen(
                userName = profileName,
                navController = navController,
                onMenuClick = { /* TODO: Handle menu click */ },
                onStartFocusClick = { duration ->
                    focusStatsViewModel.addFocusTime(duration)
                    navController.navigate("focusSession/$duration")
                },
                onSchedulesClick = { navController.navigate("schedules") },
                onChatsClick = { navController.navigate("chats") },
                onBlocksClick = { navController.navigate("blocks") },
                onQuotesClick = { navController.navigate("quotes") }
            )
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
                onStop = { navController.popBackStack() },
                onFinish = { navController.popBackStack() },
                onFocusSessionsClick = {
                    navController.navigate("dashboard")
                }
            )
        }
    }
}