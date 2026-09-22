package com.focusbubble.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.focusbubble.MainActivity
import com.focusbubble.ui.utils.UserSession
import com.focusbubble.ui.utils.PermissionHelper
import android.app.usage.UsageStatsManager
import kotlinx.coroutines.*
import android.os.CountDownTimer
import com.focusbubble.data.entities.BlockedApp
import com.focusbubble.data.entities.FocusSessionEntity
import com.focusbubble.data.entities.SessionStatus
import com.focusbubble.data.repository.BlockedAppsRepository
import com.focusbubble.data.AppDatabase
import com.focusbubble.data.dao.BlockedAppDao
import kotlinx.coroutines.flow.firstOrNull
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager




class BlockerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var repository: BlockedAppsRepository
    private lateinit var db: AppDatabase
    private var blockedApps: List<BlockedApp> = emptyList()
    private var currentBlockedApp: String? = null

    private var isPaused = false
    private var remainingTime: Long = 25 * 60 * 1000L // default 25 min
    private val notificationId = 1
    private val channelId = "focus_bubble_blocking_service"

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Something else (the blocked app) grabbed audio focus. If we're
            // still supposed to be blocking, take it right back — otherwise a
            // single successful focus request from YouTube permanently wins
            // for the rest of the session.
            if (SessionStateManager.isSessionActive(this) && !SessionStateManager.isPaused(this)) {
                Log.d("BlockerService", "🔁 Audio focus lost during active session — reclaiming")
                requestAudioFocusBlock()
            }
        }
    }
    private val tickHandler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            try {
                tick()
            } catch (e: Exception) {
                // If tick() throws and this isn't caught, postDelayed below never
                // runs, and the entire session timer silently stops forever —
                // no completion, no weekly-time credit, no congratulations screen,
                // and nothing visibly crashes to explain why. Guarantee the
                // reschedule always happens regardless.
                Log.e("BlockerService", "tick() failed, will retry next second", e)
            } finally {
                // Don't resurrect a loop that tick() itself legitimately stopped
                // (session ended or finished) — only keep going while a session
                // is still meant to be active.
                if (SessionStateManager.isSessionActive(this@BlockerService)) {
                    tickHandler.postDelayed(this, 1000)
                }
            }
        }
    }

    private val pauseResumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // The tick loop below already self-checks the persisted pause flag on
            // every tick, so it can never miss a pause/resume even if this broadcast
            // is dropped or delayed. This receiver just nudges the notification to
            // refresh immediately instead of waiting up to 1s for the next tick.
            when (intent?.action) {
                SessionStateManager.ACTION_SESSION_PAUSED -> {
                    isPaused = SessionStateManager.isPaused(this@BlockerService)
                    updateNotification()
                    abandonAudioFocusBlock()
                }
                SessionStateManager.ACTION_SESSION_RESUMED -> {
                    isPaused = SessionStateManager.isPaused(this@BlockerService)
                    updateNotification()
                    requestAudioFocusBlock()
                }
                SessionStateManager.ACTION_BLOCK_DISMISSED -> {
                    Log.d("BlockerService", "Block overlay explicitly dismissed — ready to re-block")
                    currentBlockedApp = null
                }
            }
        }
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isFreshStart = intent?.hasExtra("DURATION_MINUTES") == true

        // Only a fresh "start session" call carries this extra — a system restart
        // of the sticky service, or a notification action intent, won't. Recomputing
        // remainingTime from a default here would silently wipe out real progress.
        if (isFreshStart) {
            val durationMinutes = intent?.getIntExtra("DURATION_MINUTES", 25) ?: 25
            remainingTime = durationMinutes * 60 * 1000L
            SessionStateManager.resetPausedFlag(this)
            isPaused = false
            requestAudioFocusBlock()

            Log.d("BlockerService", "Service started with duration: $durationMinutes minutes")

            SessionStateManager.setSessionActive(this, true)
            SessionStateManager.setRemainingTime(this, remainingTime)
            SessionStateManager.setTotalDuration(this, remainingTime)
            SessionStateManager.setSessionEndTime(this, System.currentTimeMillis() + remainingTime)
        } else if (SessionStateManager.isSessionActive(this)) {
            // The service instance was (re)created without the original start extras —
            // most likely the whole process was killed and the system restarted this
            // sticky service. Without this, remainingTime silently falls back to its
            // default field value (25:00) and the CountDownTimer is never (re)started,
            // which is exactly what a permanently frozen timer looks like.
            isPaused = SessionStateManager.isPaused(this)
            remainingTime = SessionStateManager.getRemainingTime(this)
                .takeIf { it > 0L } ?: SessionStateManager.getTotalDuration(this)
            if (SessionStateManager.getSessionEndTime(this) <= 0L) {
                SessionStateManager.setSessionEndTime(this, System.currentTimeMillis() + remainingTime)
            }
            Log.d("BlockerService", "Rehydrated after restart — remaining=${remainingTime}ms paused=$isPaused")
        }

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

        // Start (or resume) ticking — safe to call unconditionally on every
        // onStartCommand since it clears any previously-scheduled tick first.
        // tick() itself checks isSessionActive/isPaused every second, so this
        // single loop correctly handles fresh starts, restarts after a process
        // kill, and pause/resume — no more separate start/cancel/restart dance.
        if (SessionStateManager.isSessionActive(this)) {
            startTicking()
        }

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
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Shared singleton (see AppDatabase.getInstance) — previously this built
        // its own separate Room.databaseBuilder(...) instance right here, which
        // risked missing writes/notifications from any other part of the app
        // using a different instance of the same underlying database file.
        db = AppDatabase.getInstance(applicationContext)
        repository = BlockedAppsRepository(db.blockedAppDao())

        // Load blocked apps from local database
        loadBlockedApps()

        // Check for Usage Stats permission
        if (!PermissionHelper.hasUsageStatsPermission(this)) {
            Log.w("BlockerService", "Usage Stats permission not granted!")
            // Service will still run but won't be able to detect apps
        }

        // ✅ Keep this service's real timer in sync with pause/resume from ANY source
        // (notification buttons, in-app buttons, or the pause sheet)
        val pauseFilter = IntentFilter().apply {
            addAction(SessionStateManager.ACTION_SESSION_PAUSED)
            addAction(SessionStateManager.ACTION_SESSION_RESUMED)
            addAction(SessionStateManager.ACTION_BLOCK_DISMISSED)
        }
        ContextCompat.registerReceiver(
            this,
            pauseResumeReceiver,
            pauseFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopTicking()
        abandonAudioFocusBlock()
        try {
            unregisterReceiver(pauseResumeReceiver)
        } catch (e: Exception) {
            // Receiver not registered — safe to ignore
        }

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
    private fun startTicking() {
        tickHandler.removeCallbacks(tickRunnable)
        tickHandler.post(tickRunnable)
    }

    private fun stopTicking() {
        tickHandler.removeCallbacks(tickRunnable)
    }

    /**
     * Runs every second for as long as the session is active. Checks the
     * persisted pause flag directly on every single tick — this is what makes
     * pause actually stop the countdown reliably, instead of depending on a
     * broadcast arriving to cancel a separate CountDownTimer.
     */
    private fun tick() {
        if (!SessionStateManager.isSessionActive(this)) {
            stopTicking()
            return
        }

        if (SessionStateManager.isPaused(this)) {
            isPaused = true
            return // frozen — remainingTime and sessionEndTime stay exactly as they were
        }
        isPaused = false

        val endTime = SessionStateManager.getSessionEndTime(this)
        val actualRemaining = if (endTime > 0) {
            (endTime - System.currentTimeMillis()).coerceAtLeast(0L)
        } else {
            remainingTime
        }
        remainingTime = actualRemaining
        SessionStateManager.setRemainingTime(this, remainingTime)
        updateNotification()

        if (actualRemaining <= 0L) {
            handleSessionFinished()
        }
    }

    private fun handleSessionFinished() {
        stopTicking()
        remainingTime = 0L
        SessionStateManager.setRemainingTime(this, 0L)
        updateNotification()

        // Credited here (not in the UI layer) so it happens even if the app was
        // closed when the session finished — a naturally-finished session always
        // completed its full planned duration.
        val completedSeconds = (SessionStateManager.getTotalDuration(this) / 1000L).toInt()
        WeeklyStatsManager.creditFocusTime(this, completedSeconds)
        SessionStateManager.setPendingCelebration(this, completedSeconds)

        // Local session-history row — this is what makes "which sessions did
        // this user actually complete" queryable on-device instead of only
        // existing as a single rolling counter (WeeklyStatsManager) with no
        // per-session detail behind it. status=COMPLETED here specifically
        // because this function only ever runs for a NATURAL finish — a
        // manual Stop is recorded separately, as STOPPED, over in
        // FocusSessionScreen.doEndSession().
        val userId = UserSession.getUserId(this)
        if (userId != -1) {
            val now = System.currentTimeMillis()
            val plannedMinutes = (SessionStateManager.getTotalDuration(this) / 60000L).toInt()
            val backendSessionId = SessionStateManager.getSessionId(this).takeIf { it != -1 }
            serviceScope.launch {
                try {
                    db.focusSessionDao().insertSession(
                        FocusSessionEntity(
                            userId = userId,
                            backendSessionId = backendSessionId,
                            plannedDurationMinutes = plannedMinutes,
                            completedSeconds = completedSeconds,
                            status = SessionStatus.COMPLETED.name,
                            startTime = now - (completedSeconds * 1000L),
                            endTime = now
                        )
                    )
                    Log.d("BlockerService", "📝 Session history row inserted (COMPLETED, ${completedSeconds}s)")
                } catch (e: Exception) {
                    Log.e("BlockerService", "Failed to insert session history row", e)
                }
            }
        }

        sendBroadcast(Intent(SessionStateManager.ACTION_SESSION_FINISHED))
        sendBroadcast(Intent(SessionStateManager.ACTION_SESSION_FINISHED))
        SessionStateManager.setSessionActive(this, false)
        stopForeground(true)
        stopSelf()
    }

    private fun togglePauseResume() {
        // Route through SessionStateManager so every listener (this service's own
        // timer, the floating widget, and the in-app UI) stays in sync from one place.
        if (SessionStateManager.isPaused(this)) {
            SessionStateManager.resumeSession(this)
        } else {
            SessionStateManager.pauseSession(this)
        }
    }

    private fun stopSession() {
        stopTicking()
        SessionStateManager.setSessionActive(this, false)
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
                // Get blocked apps from local database — scoped to whichever
                // user is currently logged in, matching the per-user isolation
                // fix applied to BlockedAppsRepository/BlockedAppsViewModel.
                val userId = UserSession.getUserId(this@BlockerService)
                val apps = repository.blockedAppsForUser(userId).firstOrNull() ?: emptyList()
                blockedApps = apps
                Log.d("BlockerService", "Loaded ${blockedApps.size} blocked apps from database (user=$userId)")
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

        val foregroundPackage = getForegroundPackageFromEvents(usageStatsManager) ?: return

        // Check if this app is blocked. Our own app is deliberately excluded from
        // ever counting as "blocked" here (rather than an early return) so that
        // moving to OUR app still falls through to the else-branch below and
        // clears currentBlockedApp — otherwise leaving a blocked app by opening
        // FocusBubble itself (e.g. via the floating timer) would skip the clear
        // and reproduce the same "only blocks once" bug for that path too.
        val isBlocked = foregroundPackage != packageName &&
                blockedApps.any { it.packageName == foregroundPackage }

        if (isBlocked) {
            if (currentBlockedApp != foregroundPackage) {
                Log.d("BlockerService", "Blocked app detected: $foregroundPackage")
                currentBlockedApp = foregroundPackage
                launchBlockOverlay(foregroundPackage)
            }
            // else: this exact blocked app is already the tracked one and its
            // overlay should already be showing — nothing to do.
        } else {
            // Foreground moved to something that ISN'T a blocked app (launcher,
            // another normal app, our own app, etc). This is the fix for the
            // "block only works once" bug: previously currentBlockedApp was ONLY
            // cleared when the user pressed Back/Continue/Emergency directly on
            // the overlay (via the ACTION_BLOCK_DISMISSED broadcast below). But
            // if the user left the blocked app any other way — Home button,
            // Recents/Overview, swiping the task away — none of that fired, so
            // currentBlockedApp stayed permanently equal to that package. Then
            // reopening that SAME blocked app later hit the equality check above
            // and silently never re-blocked it again for the rest of the session.
            // Clearing it here on ANY move to a non-blocked foreground guarantees
            // the next time that (or any) blocked app comes to the foreground,
            // isBlocked && currentBlockedApp != foregroundPackage is true again —
            // regardless of how the user left it.
            if (currentBlockedApp != null) {
                Log.d("BlockerService", "Left blocked app ($currentBlockedApp) — will re-block if reopened")
                currentBlockedApp = null
            }
        }
    }

    /**
     * Event-based foreground detection — far more reliable than queryUsageStats
     * over a tiny window, which was frequently coming back empty ("No usage stats
     * available") and made leaving a blocked app (e.g. via back button) go
     * undetected, leaving the block overlay stuck on screen.
     */
    private fun getForegroundPackageFromEvents(usageStatsManager: UsageStatsManager): String? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 10_000 // look back 10s to be safe against polling gaps

        val events = usageStatsManager.queryEvents(startTime, endTime)
        var lastForegroundPackage: String? = null
        val event = android.app.usage.UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundPackage = event.packageName
            }
        }

        if (lastForegroundPackage != null) {
            return lastForegroundPackage
        }

        // queryEvents() can go silent for extended stretches when called
        // repeatedly from a background service — confirmed by real-device logs
        // showing it return nothing for over a minute straight while the user
        // was actively switching apps. Fall back to a completely different data
        // source (aggregated usage stats, not the event stream) rather than
        // trusting queryEvents alone as the only way to ever detect anything.
        val fallback = getForegroundPackageFromUsageStats(usageStatsManager)
        if (fallback == null) {
            Log.v("BlockerService", "No foreground app detected via events or usage stats")
        }
        return fallback
    }

    private fun getForegroundPackageFromUsageStats(usageStatsManager: UsageStatsManager): String? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 15_000

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )

        return stats
            ?.filter { it.lastTimeUsed >= startTime }
            ?.maxByOrNull { it.lastTimeUsed }
            ?.packageName
    }
    private fun requestAudioFocusBlock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .build()
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = request
            val result = audioManager.requestAudioFocus(request)
            Log.d("BlockerService", "🔇 Audio focus requested: $result")
        } else {
            @Suppress("DEPRECATION")
            val result = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
            Log.d("BlockerService", "🔇 Audio focus requested (legacy): $result")
        }
    }

    private fun abandonAudioFocusBlock() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
        Log.d("BlockerService", "🔊 Audio focus released")
    }

    private fun launchBlockOverlay(packageName: String) {
        if (!Settings.canDrawOverlays(this)) {
            Log.w("BlockerService", "Cannot show overlay - permission not granted")
            return
        }

        // This is the actual fix for "Back reopens the blocked app": push it behind
        // the launcher immediately, so it's no longer the true foreground task.
        // The overlay floats on top regardless of this, but without it, the blocked
        // app remains alive underneath and Back can resurface it.
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        } catch (e: Exception) {
            Log.e("BlockerService", "❌ Failed to send blocked app home: ${e.message}", e)
        }

        // Use overlay service instead of activity to bypass Android 14 BAL restrictions
        // Give Android a moment to move the blocked app behind the launcher
// before displaying the full-screen blocking overlay.
        Handler(Looper.getMainLooper()).postDelayed({

            val overlayIntent = Intent(this, BlockOverlayService::class.java).apply {
                action = BlockOverlayService.ACTION_SHOW_OVERLAY
                putExtra(BlockOverlayService.EXTRA_PACKAGE_NAME, packageName)
                putExtra(BlockOverlayService.EXTRA_REMAINING_TIME, remainingTime)
            }

            try {
                startService(overlayIntent)
                Log.d(
                    "BlockerService",
                    "✅ Block overlay service started for: $packageName"
                )
            } catch (e: Exception) {
                Log.e(
                    "BlockerService",
                    "❌ Failed to start overlay service: ${e.message}",
                    e
                )
            }

        }, 150)
    }
}