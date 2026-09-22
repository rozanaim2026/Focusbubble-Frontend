package com.focusbubble.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.focusbubble.R

class FloatingTimerService : Service() {

    private var windowManager: WindowManager? =
        null

    private var floatingView: View? =
        null

    private var timerText: TextView? =
        null

    private var subtitleText: TextView? =
        null

    private var isViewAdded =
        false

    private var receiverRegistered =
        false

    private val handler =
        Handler(Looper.getMainLooper())

    private val pollRunnable =
        object : Runnable {
            override fun run() {
                poll()

                if (
                    SessionStateManager
                        .isSessionActive(this@FloatingTimerService)
                ) {
                    handler.postDelayed(
                        this,
                        500L
                    )
                }
            }
        }

    private val foregroundReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                poll()
            }
        }

    companion object {
        const val ACTION_SHOW_TIMER =
            "com.focusbubble.SHOW_TIMER"

        const val ACTION_HIDE_TIMER =
            "com.focusbubble.HIDE_TIMER"

        private const val TAG =
            "FloatingTimerService"
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_SHOW_TIMER -> {
                showTimer()
            }

            ACTION_HIDE_TIMER -> {
                hideTimer()
            }
        }

        return START_STICKY
    }

    private fun showTimer() {
        if (!Settings.canDrawOverlays(this)) {
            Log.w(
                TAG,
                "No overlay permission"
            )

            stopSelf()
            return
        }

        windowManager =
            getSystemService(
                WINDOW_SERVICE
            ) as WindowManager

        registerForegroundReceiverIfNeeded()

        handler.removeCallbacks(pollRunnable)
        handler.post(pollRunnable)

        poll()
    }

    private fun hideTimer() {
        handler.removeCallbacks(pollRunnable)
        removeFloatingView()
        stopSelf()
    }

    private fun registerForegroundReceiverIfNeeded() {
        if (receiverRegistered) {
            return
        }

        val filter =
            IntentFilter().apply {
                addAction(
                    SessionStateManager
                        .ACTION_APP_FOREGROUND
                )

                addAction(
                    SessionStateManager
                        .ACTION_APP_BACKGROUND
                )

                addAction(
                    SessionStateManager
                        .ACTION_SESSION_RESUMED
                )

                addAction(
                    SessionStateManager
                        .ACTION_SESSION_PAUSED
                )
            }

        ContextCompat.registerReceiver(
            this,
            foregroundReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        receiverRegistered = true
    }

    private fun poll() {
        if (
            !SessionStateManager
                .isSessionActive(this)
        ) {
            removeFloatingView()
            stopSelf()
            return
        }

        val shouldBeVisible =
            !SessionStateManager
                .isAppForeground(this) &&
                    !BlockOverlayService
                        .isOverlayVisible()

        if (
            shouldBeVisible &&
            !isViewAdded
        ) {
            addFloatingView()
        } else if (
            !shouldBeVisible &&
            isViewAdded
        ) {
            removeFloatingView()
        }

        if (isViewAdded) {
            refreshDisplay()
        }
    }

    private fun addFloatingView() {
        if (
            isViewAdded ||
            windowManager == null
        ) {
            return
        }

        val inflater =
            LayoutInflater.from(this)

        floatingView =
            inflater.inflate(
                R.layout.floating_timer_widget,
                null
            )

        timerText =
            floatingView?.findViewById(
                R.id.timer_text
            )

        subtitleText =
            floatingView?.findViewById(
                R.id.subtitle_text
            )

        floatingView?.setOnClickListener {
            if (
                SessionStateManager
                    .isPaused(this)
            ) {
                Log.d(
                    TAG,
                    "▶️ Click to Resume pressed"
                )

                SessionStateManager
                    .resumeSession(this)

                handler.post {
                    poll()
                }
            } else {
                val launchIntent =
                    packageManager
                        .getLaunchIntentForPackage(
                            packageName
                        )

                launchIntent?.let {
                    it.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK

                    startActivity(it)
                }
            }
        }

        val windowType =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {
                WindowManager.LayoutParams
                    .TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

        val params =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams
                    .FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams
                            .FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity =
                    Gravity.BOTTOM or
                            Gravity.CENTER_HORIZONTAL

                y = 100
            }

        try {
            windowManager?.addView(
                floatingView,
                params
            )

            isViewAdded = true

            Log.d(
                TAG,
                "✅ Floating timer displayed"
            )

            refreshDisplay()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "❌ Failed to add floating view",
                e
            )

            floatingView = null
            timerText = null
            subtitleText = null
            isViewAdded = false
        }
    }

    private fun refreshDisplay() {
        val paused =
            SessionStateManager
                .isPaused(this)

        val remaining =
            SessionStateManager
                .getRemainingTime(this)

        val timeText =
            formatTime(remaining)

        timerText?.text =
            if (paused) {
                "PAUSED: $timeText"
            } else {
                "Focus: $timeText"
            }

        subtitleText?.visibility =
            if (paused) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun formatTime(
        remainingMs: Long
    ): String {
        val totalSeconds =
            (remainingMs / 1000L)
                .coerceAtLeast(0L)

        val minutes =
            totalSeconds / 60L

        val seconds =
            totalSeconds % 60L

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
    }

    private fun removeFloatingView() {
        try {
            floatingView?.let {
                if (it.parent != null) {
                    windowManager?.removeView(it)
                }
            }
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Error removing floating view: " +
                        e.message
            )
        } finally {
            floatingView = null
            timerText = null
            subtitleText = null
            isViewAdded = false
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)

        if (receiverRegistered) {
            try {
                unregisterReceiver(
                    foregroundReceiver
                )
            } catch (e: Exception) {
                Log.d(
                    TAG,
                    "Foreground receiver was not registered"
                )
            }

            receiverRegistered = false
        }

        removeFloatingView()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }
}