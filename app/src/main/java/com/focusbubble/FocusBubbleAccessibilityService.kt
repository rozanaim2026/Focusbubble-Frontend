package com.focusbubble

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.focusbubble.data.AppDatabase
import com.focusbubble.service.BlockOverlayService
import com.focusbubble.service.SessionStateManager
import com.focusbubble.ui.utils.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FocusBubbleAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG =
            "FocusBubbleAccessibility"

        private const val RESUME_CHECK_1_MS =
            250L

        private const val RESUME_CHECK_2_MS =
            750L

        private const val RESUME_CHECK_3_MS =
            1500L

        private const val LEAVE_RECHECK_DELAY_MS =
            700L

        private const val HOME_IGNORE_MS =
            1500L

        private const val LEAVE_TO_HOME_SUPPRESSION_MS =
            3000L

        private const val OVERLAY_GATE_DELAY_MS =
            100L

        private const val WATCHDOG_DELAY_MS =
            250L

        private const val WATCHDOG_MAX_ATTEMPTS =
            4
    }

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Main.immediate
        )

    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val leaveHandler =
        Handler(Looper.getMainLooper())

    private var currentBlockedApp: String? =
        null

    private var lastEventPackage: String? =
        null

    private var ignoreEventsUntilElapsedMs: Long =
        0L

    private var leaveToHomeSuppressedUntilElapsedMs: Long =
        0L

    private var pendingLeaveRunnable: Runnable? =
        null

    private val sessionStateReceiver =
        object : BroadcastReceiver() {

            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                when (intent?.action) {
                    SessionStateManager
                        .ACTION_SESSION_RESUMED -> {

                        Log.d(
                            TAG,
                            "▶️ Session resumed; " +
                                    "checking foreground"
                        )

                        leaveToHomeSuppressedUntilElapsedMs =
                            0L

                        cancelPendingLeave()

                        currentBlockedApp = null
                        lastEventPackage = null

                        checkForegroundAfterResume()
                    }

                    SessionStateManager
                        .ACTION_BLOCK_DISMISSED -> {

                        Log.d(
                            TAG,
                            "Block dismissed; clearing state"
                        )

                        cancelPendingLeave()

                        currentBlockedApp = null
                        lastEventPackage = null
                    }

                    SessionStateManager
                        .ACTION_LEAVE_TO_HOME -> {

                        leaveToHomeSuppressedUntilElapsedMs =
                            android.os.SystemClock
                                .elapsedRealtime() +
                                    LEAVE_TO_HOME_SUPPRESSION_MS

                        cancelPendingLeave()

                        currentBlockedApp = null
                        lastEventPackage = null

                        Log.d(
                            TAG,
                            "🏠 Leave-to-Home suppression enabled"
                        )
                    }
                }
            }
        }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val filter =
            IntentFilter().apply {
                addAction(
                    SessionStateManager
                        .ACTION_SESSION_RESUMED
                )

                addAction(
                    SessionStateManager
                        .ACTION_BLOCK_DISMISSED
                )

                addAction(
                    SessionStateManager
                        .ACTION_LEAVE_TO_HOME
                )
            }

        ContextCompat.registerReceiver(
            this,
            sessionStateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        Log.d(
            TAG,
            "Accessibility service connected"
        )

        checkForegroundIfSessionActive()
    }

    private fun isLeaveToHomeSuppressed(): Boolean {
        return android.os.SystemClock
            .elapsedRealtime() <
                leaveToHomeSuppressedUntilElapsedMs
    }

    private fun checkForegroundIfSessionActive() {
        if (
            SessionStateManager
                .isSessionActive(this) &&
            !SessionStateManager.isPaused(this)
        ) {
            checkForegroundAfterResume()
        }
    }

    override fun onAccessibilityEvent(
        event: AccessibilityEvent?
    ) {
        if (event == null) {
            return
        }

        if (
            event.eventType !=
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) {
            return
        }

        if (
            android.os.SystemClock.elapsedRealtime() <
            ignoreEventsUntilElapsedMs
        ) {
            Log.d(
                TAG,
                "Ignoring event during navigation window"
            )
            return
        }

        val packageName =
            event.packageName
                ?.toString()
                ?: return

        Log.d(
            TAG,
            "Window event package=$packageName"
        )

        if (packageName == this.packageName) {
            return
        }

        if (packageName == lastEventPackage) {
            return
        }

        lastEventPackage = packageName

        evaluateForegroundPackage(packageName)
    }

    private fun checkForegroundAfterResume(
        attempt: Int = 0
    ) {
        if (
            !SessionStateManager
                .isSessionActive(this) ||
            SessionStateManager.isPaused(this)
        ) {
            Log.d(
                TAG,
                "Resume check stopped"
            )
            return
        }

        val foregroundPackage =
            rootInActiveWindow
                ?.packageName
                ?.toString()

        Log.d(
            TAG,
            "Resume foreground attempt=$attempt: " +
                    "$foregroundPackage"
        )

        if (
            foregroundPackage != null &&
            foregroundPackage != this.packageName
        ) {
            lastEventPackage = null

            evaluateForegroundPackage(
                foregroundPackage
            )

            return
        }

        when (attempt) {
            0 -> {
                mainHandler.postDelayed(
                    {
                        checkForegroundAfterResume(1)
                    },
                    RESUME_CHECK_1_MS
                )
            }

            1 -> {
                mainHandler.postDelayed(
                    {
                        checkForegroundAfterResume(2)
                    },
                    RESUME_CHECK_2_MS
                )
            }

            2 -> {
                mainHandler.postDelayed(
                    {
                        checkForegroundAfterResume(3)
                    },
                    RESUME_CHECK_3_MS
                )
            }

            else -> {
                Log.w(
                    TAG,
                    "Foreground unavailable after Resume"
                )
            }
        }
    }

    private fun evaluateForegroundPackage(
        packageName: String
    ) {
        if (isLeaveToHomeSuppressed()) {
            Log.d(
                TAG,
                "Ignoring $packageName after Leave to Home"
            )
            return
        }

        serviceScope.launch {
            val context =
                this@FocusBubbleAccessibilityService

            val sessionActive =
                SessionStateManager
                    .isSessionActive(context)

            val sessionPaused =
                SessionStateManager
                    .isPaused(context)

            if (!sessionActive || sessionPaused) {
                cancelPendingLeave()
                currentBlockedApp = null
                return@launch
            }

            val userId =
                UserSession.getUserId(context)

            if (userId == -1) {
                Log.w(
                    TAG,
                    "No valid user ID"
                )
                return@launch
            }

            val blockedApps =
                AppDatabase
                    .getInstance(context)
                    .blockedAppDao()
                    .getAllBlockedApps(userId)
                    .first()

            val isBlocked =
                blockedApps.any {
                    it.packageName == packageName
                }

            if (isBlocked) {
                cancelPendingLeave()

                currentBlockedApp =
                    packageName

                val overlayVisible =
                    BlockOverlayService
                        .isOverlayVisible()

                Log.d(
                    TAG,
                    "Blocked package=$packageName, " +
                            "overlayVisible=$overlayVisible"
                )

                if (!overlayVisible) {
                    enforceBlock(packageName)
                }
            } else {
                currentBlockedApp?.let {
                    scheduleLeaveCheck(it)
                }
            }
        }
    }

    private fun scheduleLeaveCheck(
        blockedPackage: String
    ) {
        cancelPendingLeave()

        val runnable =
            Runnable {
                if (isLeaveToHomeSuppressed()) {
                    Log.d(
                        TAG,
                        "Leave check ignored after " +
                                "Leave to Home"
                    )

                    pendingLeaveRunnable = null
                    return@Runnable
                }

                val foregroundPackage =
                    rootInActiveWindow
                        ?.packageName
                        ?.toString()

                val sessionActive =
                    SessionStateManager
                        .isSessionActive(this)

                val sessionPaused =
                    SessionStateManager
                        .isPaused(this)

                Log.d(
                    TAG,
                    "Leave check: " +
                            "blocked=$blockedPackage, " +
                            "foreground=$foregroundPackage"
                )

                if (!sessionActive || sessionPaused) {
                    currentBlockedApp = null
                    pendingLeaveRunnable = null
                    return@Runnable
                }

                if (
                    foregroundPackage == blockedPackage
                ) {
                    if (
                        !BlockOverlayService
                            .isOverlayVisible()
                    ) {
                        enforceBlock(blockedPackage)
                    }

                    pendingLeaveRunnable = null
                    return@Runnable
                }

                if (
                    foregroundPackage == null ||
                    foregroundPackage == this.packageName
                ) {
                    pendingLeaveRunnable = null
                    return@Runnable
                }

                startService(
                    Intent(
                        this,
                        BlockOverlayService::class.java
                    ).apply {
                        action =
                            BlockOverlayService
                                .ACTION_HIDE_OVERLAY
                    }
                )

                currentBlockedApp = null
                pendingLeaveRunnable = null
            }

        pendingLeaveRunnable = runnable

        leaveHandler.postDelayed(
            runnable,
            LEAVE_RECHECK_DELAY_MS
        )
    }

    private fun cancelPendingLeave() {
        pendingLeaveRunnable?.let {
            leaveHandler.removeCallbacks(it)
        }

        pendingLeaveRunnable = null
    }

    private fun enforceBlock(
        packageName: String
    ) {
        if (isLeaveToHomeSuppressed()) {
            Log.d(
                TAG,
                "Not enforcing $packageName after Leave"
            )
            return
        }

        if (
            !SessionStateManager
                .isSessionActive(this) ||
            SessionStateManager.isPaused(this)
        ) {
            Log.d(
                TAG,
                "Not enforcing $packageName; " +
                        "session inactive or paused"
            )
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            Log.w(
                TAG,
                "Overlay permission is missing"
            )
            return
        }

        val remainingTime =
            SessionStateManager
                .getRemainingTime(this)

        if (remainingTime <= 0L) {
            return
        }

        currentBlockedApp =
            packageName

        FocusBubbleNotificationListener
            .instance
            ?.pauseMediaFor(packageName)

        val overlayIntent =
            Intent(
                this,
                BlockOverlayService::class.java
            ).apply {
                action =
                    BlockOverlayService
                        .ACTION_SHOW_OVERLAY

                putExtra(
                    BlockOverlayService
                        .EXTRA_PACKAGE_NAME,
                    packageName
                )

                putExtra(
                    BlockOverlayService
                        .EXTRA_REMAINING_TIME,
                    remainingTime
                )
            }

        try {
            startService(overlayIntent)

            Log.d(
                TAG,
                "🛡️ Overlay requested for $packageName"
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to start overlay",
                e
            )
            return
        }

        mainHandler.postDelayed(
            {
                if (
                    isLeaveToHomeSuppressed() ||
                    !SessionStateManager
                        .isSessionActive(this) ||
                    SessionStateManager.isPaused(this)
                ) {
                    return@postDelayed
                }

                val overlayVisible =
                    BlockOverlayService
                        .isOverlayVisible()

                if (!overlayVisible) {
                    startService(overlayIntent)
                }

                ignoreEventsUntilElapsedMs =
                    android.os.SystemClock
                        .elapsedRealtime() +
                            HOME_IGNORE_MS

                val homeResult =
                    performGlobalAction(
                        GLOBAL_ACTION_HOME
                    )

                Log.d(
                    TAG,
                    "🏠 HOME sent for $packageName: " +
                            homeResult
                )
            },
            OVERLAY_GATE_DELAY_MS
        )

        mainHandler.postDelayed(
            {
                verifyOverlayAfterBlock(packageName)
            },
            WATCHDOG_DELAY_MS
        )
    }

    private fun verifyOverlayAfterBlock(
        packageName: String,
        attempt: Int = 0
    ) {
        if (
            isLeaveToHomeSuppressed() ||
            !SessionStateManager
                .isSessionActive(this) ||
            SessionStateManager.isPaused(this)
        ) {
            return
        }

        val overlayVisible =
            BlockOverlayService
                .isOverlayVisible()

        val foregroundPackage =
            rootInActiveWindow
                ?.packageName
                ?.toString()

        Log.d(
            TAG,
            "Overlay watchdog attempt=$attempt: " +
                    "blocked=$packageName, " +
                    "foreground=$foregroundPackage, " +
                    "overlay=$overlayVisible"
        )

        if (
            foregroundPackage == packageName &&
            !overlayVisible
        ) {
            enforceBlock(packageName)
            return
        }

        if (
            attempt < WATCHDOG_MAX_ATTEMPTS
        ) {
            mainHandler.postDelayed(
                {
                    verifyOverlayAfterBlock(
                        packageName,
                        attempt + 1
                    )
                },
                WATCHDOG_DELAY_MS
            )
        }
    }

    override fun onKeyEvent(
        event: android.view.KeyEvent
    ): Boolean {
        if (
            event.keyCode ==
            android.view.KeyEvent.KEYCODE_BACK &&
            event.action ==
            android.view.KeyEvent.ACTION_UP
        ) {
            if (
                BlockOverlayService
                    .isOverlayVisible()
            ) {
                hideOverlayAndGoHome()
                return true
            }
        }

        return super.onKeyEvent(event)
    }

    private fun hideOverlayAndGoHome() {
        startService(
            Intent(
                this,
                BlockOverlayService::class.java
            ).apply {
                action =
                    BlockOverlayService
                        .ACTION_HIDE_OVERLAY
            }
        )

        sendBroadcast(
            Intent(
                SessionStateManager
                    .ACTION_BLOCK_DISMISSED
            ).apply {
                setPackage(packageName)
            }
        )

        currentBlockedApp = null
        lastEventPackage = null

        cancelPendingLeave()

        performGlobalAction(
            GLOBAL_ACTION_HOME
        )
    }

    override fun onInterrupt() {
        Log.d(
            TAG,
            "Accessibility service interrupted"
        )
    }

    override fun onDestroy() {
        cancelPendingLeave()

        serviceScope.cancel()

        mainHandler.removeCallbacksAndMessages(null)
        leaveHandler.removeCallbacksAndMessages(null)

        try {
            unregisterReceiver(
                sessionStateReceiver
            )
        } catch (e: Exception) {
            Log.d(
                TAG,
                "Session receiver was not registered"
            )
        }

        super.onDestroy()
    }
}