package com.focusbubble.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.focusbubble.MainActivity
import com.focusbubble.ui.BlockOverlayActivity
import com.focusbubble.ui.utils.UserSession
import com.focusbubble.ui.utils.PermissionHelper
import android.app.usage.UsageStatsManager
import kotlinx.coroutines.*
import android.os.CountDownTimer
import com.focusbubble.data.entities.BlockedApp
import com.focusbubble.data.repository.BlockedAppsRepository
import com.focusbubble.data.AppDatabase
import com.focusbubble.data.dao.BlockedAppDao
import kotlinx.coroutines.flow.firstOrNull
import androidx.room.Room




class BlockerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var repository: BlockedAppsRepository
    private var blockedApps: List<BlockedApp> = emptyList()
    private var currentBlockedApp: String? = null

    private var isPaused = false
    private var remainingTime: Long = 25 * 60 * 1000L // default 25 min
    private val notificationId = 1
    private val channelId = "focus_bubble_blocking_service"
    private var timer: CountDownTimer? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val durationMinutes = intent?.getIntExtra("DURATION_MINUTES", 25) ?: 25
        remainingTime = durationMinutes * 60 * 1000L

        Log.d("BlockerService", "Service started with duration: $durationMinutes minutes")

        // ✅ Initialize SessionStateManager
        SessionStateManager.setSessionActive(this, true)
        SessionStateManager.setRemainingTime(this, remainingTime)
        SessionStateManager.setTotalDuration(this, remainingTime)

        // Create notification channel and start foreground
        createNotificationChannel()
        
        // Start foreground with special use type for Android 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                notificationId,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(notificationId, buildNotification())
        }

        // ✅ Start floating timer widget
        try {
            val floatingIntent = Intent(this, FloatingTimerService::class.java).apply {
                action = FloatingTimerService.ACTION_SHOW_TIMER
            }
            startService(floatingIntent)
            Log.d("BlockerService", "✅ Floating timer started")
        } catch (e: Exception) {
            Log.e("BlockerService", "❌ Failed to start floating timer: ${e.message}")
        }

        // Start the timer
        startTimer(remainingTime)

        // Handle action buttons
        when (intent?.action) {
            "PAUSE_SESSION" -> togglePauseResume()
            "STOP_SESSION" -> stopSession()
        }

        // Reload blocked apps (in case they were updated)
        loadBlockedApps()

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BlockerService", "Service created")

        // Initialize repository
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "blocked_apps_db"
        )
            .allowMainThreadQueries() // Allow main thread for service
            .fallbackToDestructiveMigration() // Handle schema changes
            .build()
        repository = BlockedAppsRepository(database.blockedAppDao())

        // Load blocked apps from local database
        loadBlockedApps()

        // Check for Usage Stats permission
        if (!PermissionHelper.hasUsageStatsPermission(this)) {
            Log.w("BlockerService", "Usage Stats permission not granted!")
            // Service will still run but won't be able to detect apps
        }

        // Start monitoring foreground apps
        serviceScope.launch {
            while (isActive) {
                if (!isPaused) {
                    checkForegroundApp()
                }
                delay(2000) // Check every 2 seconds
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        timer?.cancel()
        
        // ✅ Clear session state and stop floating timer
        SessionStateManager.clearSession(this)
        val floatingIntent = Intent(this, FloatingTimerService::class.java).apply {
            action = FloatingTimerService.ACTION_HIDE_TIMER
        }
        stopService(floatingIntent)
        
        Log.d("BlockerService", "Service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // Timer & Notification
    private fun startTimer(timeMillis: Long) {
        timer?.cancel()
        if (isPaused) return

        timer = object : CountDownTimer(timeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                // ✅ Update SessionStateManager so floating timer can update
                SessionStateManager.setRemainingTime(this@BlockerService, remainingTime)
                updateNotification()
            }

            override fun onFinish() {
                remainingTime = 0L
                SessionStateManager.setRemainingTime(this@BlockerService, 0L)
                updateNotification()
                stopForeground(true)
                stopSelf()
            }
        }.start()
    }

    private fun togglePauseResume() {
        isPaused = !isPaused
        if (!isPaused) startTimer(remainingTime)
        else timer?.cancel()
        updateNotification()
    }

    private fun stopSession() {
        timer?.cancel()
        stopForeground(true)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val pauseIntent = Intent(this, BlockerService::class.java).apply {
            action = "PAUSE_SESSION"
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 0, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, BlockerService::class.java).apply {
            action = "STOP_SESSION"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minutes = (remainingTime / 1000) / 60
        val seconds = (remainingTime / 1000) % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        val blockedCount = blockedApps.size

        val pauseText = if (isPaused) "Resume" else "Pause"

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Focus Session")
            .setContentText("$timeText remaining • $blockedCount apps blocked")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(mainPendingIntent)
            .addAction(0, pauseText, pausePendingIntent)
            .addAction(0, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, buildNotification())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                "Focus Bubble Blocking Service",
                NotificationManager.IMPORTANCE_LOW
            )
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(chan)
        }
    }

    // App Blocking - Load from LOCAL database
    private fun loadBlockedApps() {
        serviceScope.launch {
            try {
                // Get blocked apps from local database
                val apps = repository.blockedApps.firstOrNull() ?: emptyList()
                blockedApps = apps
                Log.d("BlockerService", "Loaded ${blockedApps.size} blocked apps from database")
                blockedApps.forEach { app ->
                    Log.d("BlockerService", "Blocking: ${app.appName} (${app.packageName})")
                }
            } catch (e: Exception) {
                Log.e("BlockerService", "Error loading blocked apps: ${e.message}")
            }
        }
    }

    private fun checkForegroundApp() {
        // ✅ Don't block apps if session is paused
        if (SessionStateManager.isPaused(this)) {
            Log.v("BlockerService", "Session paused - not blocking apps")
            return
        }
        
        // Check permission first
        if (!PermissionHelper.hasUsageStatsPermission(this)) {
            Log.w("BlockerService", "Cannot check foreground app - no Usage Stats permission")
            return
        }

        val usageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 2000

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        if (usageStatsList.isNullOrEmpty()) {
            Log.v("BlockerService", "No usage stats available")
            return
        }

        val recentApp = usageStatsList.maxByOrNull { it.lastTimeUsed }
        recentApp?.let { stats ->
            val foregroundPackage = stats.packageName
            
            // Skip our own app
            if (foregroundPackage == packageName) return
            
            // Check if this app is blocked
            val isBlocked = blockedApps.any { it.packageName == foregroundPackage }

            if (isBlocked && currentBlockedApp != foregroundPackage) {
                Log.d("BlockerService", "Blocked app detected: $foregroundPackage")
                currentBlockedApp = foregroundPackage
                launchBlockOverlay(foregroundPackage)
            } else if (!isBlocked && currentBlockedApp == foregroundPackage) {
                currentBlockedApp = null
            }
        }
    }

    private fun launchBlockOverlay(packageName: String) {
        if (!Settings.canDrawOverlays(this)) {
            Log.w("BlockerService", "Cannot show overlay - permission not granted")
            return
        }

        // Use overlay service instead of activity to bypass Android 14 BAL restrictions
        val overlayIntent = Intent(this, BlockOverlayService::class.java).apply {
            action = BlockOverlayService.ACTION_SHOW_OVERLAY
            putExtra(BlockOverlayService.EXTRA_PACKAGE_NAME, packageName)
            putExtra(BlockOverlayService.EXTRA_REMAINING_TIME, remainingTime)
        }
        
        try {
            startService(overlayIntent)
            Log.d("BlockerService", "✅ Block overlay service started for: $packageName")
        } catch (e: Exception) {
            Log.e("BlockerService", "❌ Failed to start overlay service: ${e.message}", e)
        }
    }
}
