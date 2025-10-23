package com.focusbubble.service

import android.app.ActivityManager
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

/**
 * Service that shows a persistent Compose-based overlay window
 * Matches FocusSessionScreen design with circular timer
 * Stays alive throughout focus session - truly persistent blocking!
 */
class BlockOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var timer: CountDownTimer? = null
    private var remainingTimeMillis: Long = 0L
    private var totalDurationMillis: Long = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var currentBlockedPackage: String? = null

    companion object {
        const val ACTION_SHOW_OVERLAY = "SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "HIDE_OVERLAY"
        const val ACTION_EMERGENCY_USE = "EMERGENCY_USE"
        const val EXTRA_PACKAGE_NAME = "PACKAGE_NAME"
        const val EXTRA_REMAINING_TIME = "REMAINING_TIME"
        
        private var instance: BlockOverlayService? = null
        
        fun isRunning(): Boolean = instance != null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("BlockOverlayService", "Service created - ready for persistent blocking")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_OVERLAY -> {
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: "Unknown App"
                remainingTimeMillis = intent.getLongExtra(EXTRA_REMAINING_TIME, 25 * 60 * 1000L)
                
                // Store total duration if not set yet
                if (totalDurationMillis == 0L) {
                    totalDurationMillis = remainingTimeMillis
                }
                
                Log.d("BlockOverlayService", "Showing overlay for: $packageName (${remainingTimeMillis/1000}s remaining)")
                
                if (!Settings.canDrawOverlays(this)) {
                    Log.e("BlockOverlayService", "Cannot draw overlays - permission not granted")
                    return START_STICKY
                }

                showOverlay(packageName)
            }
            ACTION_HIDE_OVERLAY -> {
                hideOverlay()
            }
            ACTION_EMERGENCY_USE -> {
                handleEmergencyUse()
            }
        }
        
        return START_STICKY  // Keep service alive for persistent blocking!
    }

    private fun showOverlay(packageName: String) {
        try {
            // Store current blocked package
            currentBlockedPackage = packageName
            
            // If overlay already showing, just update it
            if (overlayView != null && overlayView!!.parent != null) {
                Log.d("BlockOverlayService", "Overlay already visible, updating content...")
                updateOverlayContent(packageName)
                return
            }

            // Remove any existing overlay first
            removeOverlay()

            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val appName = getAppName(packageName)
            val remainingSeconds = (remainingTimeMillis / 1000).toInt()
            val totalSeconds = (totalDurationMillis / 1000).toInt()
            
            // Create Compose-based overlay using factory
            overlayView = BlockOverlayViewFactory.create(
                context = this,
                appName = appName,
                remainingTimeSeconds = remainingSeconds,
                totalDurationSeconds = totalSeconds,
                onContinue = {
                    Log.d("BlockOverlayService", "✅ User chose to continue focus")
                    
                    // Force close the blocked app so it can't be resumed from recents
                    currentBlockedPackage?.let { pkg ->
                        forceCloseApp(pkg)
                    }
                    
                    hideOverlay()
                    
                    // Go to home
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(homeIntent)
                    // DON'T stop service - keep it running for next blocked app!
                },
                onEmergency = {
                    Log.d("BlockOverlayService", "🚨 User requested emergency use")
                    handleEmergencyUse()
                }
            )

            // Window parameters
            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.START

            // Add the overlay view
            windowManager?.addView(overlayView, params)
            Log.d("BlockOverlayService", "✅ Compose overlay displayed for: $appName")
            
            // Start timer to update UI every second
            startTimerUpdates()

        } catch (e: Exception) {
            Log.e("BlockOverlayService", "❌ Error showing overlay: ${e.message}", e)
            // DON'T stop service - just log error and keep running
        }
    }

    private fun startTimerUpdates() {
        timer?.cancel()
        
        timer = object : CountDownTimer(remainingTimeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimeMillis = millisUntilFinished
                // Timer countdown happens in BlockerService
                // This just tracks remaining time
            }

            override fun onFinish() {
                Log.d("BlockOverlayService", "⏰ Focus session completed!")
                removeOverlay()
                stopSelf() // Only stop when session truly ends
            }
        }
        timer?.start()
    }

    private fun hideOverlay() {
        // DON'T cancel timer - keep it running!
        // DON'T stop service - keep it alive for next blocked app!
        removeOverlay()
        Log.d("BlockOverlayService", "💡 Overlay hidden but service still running (persistent!)")
    }

    private fun updateOverlayContent(packageName: String) {
        // Re-create overlay with updated content
        removeOverlay()
        
        handler.postDelayed({
            showOverlay(packageName)
        }, 100) // Small delay to ensure clean recreation
    }

    private fun handleEmergencyUse() {
        timer?.cancel()
        removeOverlay()
        
        // ✅ PAUSE SESSION (don't stop it!)
        SessionStateManager.pauseSession(this)
        Log.d("BlockOverlayService", "⏸️ Emergency use - session PAUSED (not stopped)")
        
        // DON'T stop BlockerService - it should pause monitoring
        // DON'T stop overlay service - keep it ready for resume
        stopSelf()
    }
    
    private fun forceCloseApp(packageName: String) {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.killBackgroundProcesses(packageName)
            Log.d("BlockOverlayService", "🔪 Force closed app: $packageName")
        } catch (e: Exception) {
            Log.e("BlockOverlayService", "❌ Failed to force close app: ${e.message}", e)
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun removeOverlay() {
        try {
            if (overlayView != null && overlayView!!.parent != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
                Log.d("BlockOverlayService", "Overlay removed from window")
            }
        } catch (e: Exception) {
            Log.e("BlockOverlayService", "Error removing overlay: ${e.message}", e)
            overlayView = null // Clear reference anyway
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        timer?.cancel()
        handler.removeCallbacksAndMessages(null)
        removeOverlay()
        Log.d("BlockOverlayService", "Service destroyed (session ended or emergency)")
    }
}
