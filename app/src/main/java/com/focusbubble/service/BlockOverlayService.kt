package com.focusbubble.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import com.focusbubble.ui.overlay.BlockOverlayViewFactory

class BlockOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var timer: CountDownTimer? = null

    private var remainingTimeMillis: Long = 0L
    private var totalDurationMillis: Long = 0L

    private val handler =
        Handler(Looper.getMainLooper())

    private var currentBlockedPackage: String? = null

    companion object {
        const val ACTION_SHOW_OVERLAY =
            "com.focusbubble.action.SHOW_OVERLAY"

        const val ACTION_HIDE_OVERLAY =
            "com.focusbubble.action.HIDE_OVERLAY"

        const val ACTION_EMERGENCY_USE =
            "com.focusbubble.action.EMERGENCY_USE"

        const val EXTRA_PACKAGE_NAME =
            "com.focusbubble.extra.PACKAGE_NAME"

        const val EXTRA_REMAINING_TIME =
            "com.focusbubble.extra.REMAINING_TIME"

        private const val TAG =
            "BlockOverlayService"

        private var instance: BlockOverlayService? = null

        fun isRunning(): Boolean {
            return instance != null
        }

        fun isOverlayVisible(): Boolean {
            return instance
                ?.isOverlayCurrentlyShown()
                ?: false
        }
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        Log.d(
            TAG,
            "Overlay service created"
        )
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? {
        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        when (intent?.action) {
            ACTION_SHOW_OVERLAY -> {
                handleShowOverlay(intent)
            }

            ACTION_HIDE_OVERLAY -> {
                Log.d(
                    TAG,
                    "Hide overlay action received"
                )

                hideOverlay()
            }

            ACTION_EMERGENCY_USE -> {
                Log.d(
                    TAG,
                    "Emergency action received"
                )

                handleEmergencyUse()
            }

            else -> {
                Log.d(
                    TAG,
                    "No recognized action"
                )
            }
        }

        return START_STICKY
    }

    private fun handleShowOverlay(
        intent: Intent
    ) {
        val packageName =
            intent.getStringExtra(
                EXTRA_PACKAGE_NAME
            ) ?: return

        remainingTimeMillis =
            intent.getLongExtra(
                EXTRA_REMAINING_TIME,
                25L * 60L * 1000L
            )

        if (remainingTimeMillis <= 0L) {
            Log.w(
                TAG,
                "Remaining time is zero"
            )
            return
        }

        if (totalDurationMillis <= 0L) {
            totalDurationMillis =
                remainingTimeMillis
        }

        if (!Settings.canDrawOverlays(this)) {
            Log.e(
                TAG,
                "Overlay permission is not granted"
            )
            return
        }

        showOverlay(packageName)
    }

    private fun showOverlay(
        packageName: String
    ) {
        try {
            currentBlockedPackage =
                packageName

            if (
                overlayView != null &&
                overlayView?.parent != null
            ) {
                Log.d(
                    TAG,
                    "Overlay already visible; keeping window"
                )
                return
            }

            windowManager =
                getSystemService(
                    Context.WINDOW_SERVICE
                ) as WindowManager

            val appName =
                getAppName(packageName)

            val remainingSeconds =
                (remainingTimeMillis / 1000L)
                    .toInt()
                    .coerceAtLeast(0)

            val totalSeconds =
                (totalDurationMillis / 1000L)
                    .toInt()
                    .coerceAtLeast(1)

            overlayView =
                BlockOverlayViewFactory.create(
                    context = this,
                    appName = appName,
                    remainingTimeSeconds =
                        remainingSeconds,
                    totalDurationSeconds =
                        totalSeconds,

                    onLeave = {
                        Log.d(
                            TAG,
                            "Leave to Home clicked; " +
                                    "keeping session active"
                        )

                        hideOverlay()

                        /*
                         * Leave does NOT pause the session.
                         * It only tells the accessibility service to
                         * suppress immediate re-detection while Home
                         * finishes opening.
                         */
                        sendBroadcast(
                            Intent(
                                SessionStateManager
                                    .ACTION_BLOCK_DISMISSED
                            ).setPackage(
                                this@BlockOverlayService.packageName
                            )
                        )

                        goToHomeUsingActivityIntent()
                    },

                    onEmergency = {
                        Log.d(
                            TAG,
                            "Emergency Use clicked"
                        )

                        /*
                         * Only Emergency calls handleEmergencyUse().
                         */
                        handleEmergencyUse()
                    }
                )

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

            val windowFlags =
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

            val params =
                WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    windowType,
                    windowFlags,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity =
                        Gravity.TOP or Gravity.START

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.P
                    ) {
                        layoutInDisplayCutoutMode =
                            WindowManager.LayoutParams
                                .LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }

                    if (
                        Build.VERSION.SDK_INT >=
                        Build.VERSION_CODES.R
                    ) {
                        setFitInsetsTypes(0)
                    }
                }

            windowManager?.addView(
                overlayView,
                params
            )

            Log.d(
                TAG,
                "✅ Full-screen overlay displayed for $appName"
            )

            startTimerUpdates()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to display overlay",
                e
            )

            removeOverlay()
        }
    }

    private fun goToHomeUsingActivityIntent() {
        try {
            val homeIntent =
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK
                }

            startActivity(homeIntent)

            Log.d(
                TAG,
                "🏠 Home intent started"
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Could not start Home intent",
                e
            )
        }
    }

    private fun startTimerUpdates() {
        timer?.cancel()

        timer =
            object : CountDownTimer(
                remainingTimeMillis,
                1000L
            ) {
                override fun onTick(
                    millisUntilFinished: Long
                ) {
                    remainingTimeMillis =
                        millisUntilFinished
                }

                override fun onFinish() {
                    Log.d(
                        TAG,
                        "⏰ Focus session completed"
                    )

                    removeOverlay()

                    currentBlockedPackage = null
                    remainingTimeMillis = 0L
                    totalDurationMillis = 0L

                    stopSelf()
                }
            }

        timer?.start()
    }

    private fun hideOverlay() {
        timer?.cancel()
        timer = null

        removeOverlay()

        Log.d(
            TAG,
            "Overlay hidden"
        )
    }

    private fun handleEmergencyUse() {
        timer?.cancel()
        timer = null

        val packageToReopen =
            currentBlockedPackage

        removeOverlay()

        /*
         * Emergency is the only action that pauses.
         */
        SessionStateManager.pauseSession(this)

        sendBroadcast(
            Intent(
                SessionStateManager
                    .ACTION_BLOCK_DISMISSED
            ).setPackage(
                packageName
            )
        )

        Log.d(
            TAG,
            "⏸️ Emergency Use; session paused"
        )

        if (packageToReopen != null) {
            try {
                val launchIntent =
                    packageManager
                        .getLaunchIntentForPackage(
                            packageToReopen
                        )

                if (launchIntent != null) {
                    launchIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )

                    startActivity(launchIntent)

                    Log.d(
                        TAG,
                        "↩️ Reopened $packageToReopen"
                    )
                } else {
                    Log.w(
                        TAG,
                        "No launch intent found for " +
                                packageToReopen
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Could not reopen blocked app",
                    e
                )
            }
        }

        stopSelf()
    }

    private fun getAppName(
        packageName: String
    ): String {
        return try {
            val appInfo =
                packageManager
                    .getApplicationInfo(
                        packageName,
                        0
                    )

            packageManager
                .getApplicationLabel(appInfo)
                .toString()
        } catch (e: Exception) {
            packageName
        }
    }

    fun isOverlayCurrentlyShown(): Boolean {
        return overlayView?.parent != null
    }

    private fun removeOverlay() {
        val view =
            overlayView

        try {
            if (
                view != null &&
                view.parent != null
            ) {
                windowManager?.removeView(view)

                Log.d(
                    TAG,
                    "Overlay removed from window"
                )
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Could not remove overlay",
                e
            )
        } finally {
            overlayView = null
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        timer = null

        handler.removeCallbacksAndMessages(null)

        removeOverlay()

        currentBlockedPackage = null
        remainingTimeMillis = 0L
        totalDurationMillis = 0L

        instance = null

        Log.d(
            TAG,
            "Overlay service destroyed"
        )

        super.onDestroy()
    }
}