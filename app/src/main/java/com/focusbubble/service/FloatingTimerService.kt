package com.focusbubble.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.focusbubble.R

/**
 * Floating timer widget that appears on home screen during focus session
 * Shows remaining time and session status
 */
class FloatingTimerService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var timerText: TextView? = null
    
    companion object {
        const val ACTION_SHOW_TIMER = "com.focusbubble.SHOW_TIMER"
        const val ACTION_HIDE_TIMER = "com.focusbubble.HIDE_TIMER"
        private const val TAG = "FloatingTimerService"
    }
    
    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SessionStateManager.ACTION_UPDATE_TIMER -> {
                    val remaining = intent.getLongExtra(SessionStateManager.EXTRA_REMAINING_TIME, 0L)
                    updateTimer(remaining)
                }
                SessionStateManager.ACTION_SESSION_PAUSED -> {
                    updatePausedState(true)
                }
                SessionStateManager.ACTION_SESSION_RESUMED -> {
                    updatePausedState(false)
                }
                SessionStateManager.ACTION_SESSION_STOPPED -> {
                    removeFloatingView()
                    stopSelf()
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Register receiver for timer updates
        val filter = IntentFilter().apply {
            addAction(SessionStateManager.ACTION_UPDATE_TIMER)
            addAction(SessionStateManager.ACTION_SESSION_PAUSED)
            addAction(SessionStateManager.ACTION_SESSION_RESUMED)
            addAction(SessionStateManager.ACTION_SESSION_STOPPED)
        }
        // Use ContextCompat for API 33+ compatibility (RECEIVER_NOT_EXPORTED required)
        ContextCompat.registerReceiver(
            this,
            updateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_TIMER -> {
                if (!Settings.canDrawOverlays(this)) {
                    Log.w(TAG, "No overlay permission")
                    stopSelf()
                    return START_NOT_STICKY
                }
                showFloatingTimer()
            }
            ACTION_HIDE_TIMER -> {
                removeFloatingView()
                stopSelf()
            }
        }
        return START_STICKY
    }
    
    private fun showFloatingTimer() {
        if (floatingView != null) return
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Inflate the floating widget layout
        val inflater = LayoutInflater.from(this)
        floatingView = inflater.inflate(R.layout.floating_timer_widget, null)
        
        timerText = floatingView?.findViewById(R.id.timer_text)
        
        // Make view clickable to open app
        floatingView?.setOnClickListener {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.let {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(it)
            }
        }
        
        // Window parameters
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        
        // Position at bottom center
        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.y = 100 // 100px from bottom
        
        try {
            windowManager?.addView(floatingView, params)
            Log.d(TAG, "✅ Floating timer displayed")
            
            // Show initial time
            val remaining = SessionStateManager.getRemainingTime(this)
            updateTimer(remaining)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to add floating view: ${e.message}", e)
            floatingView = null
        }
    }
    
    private fun updateTimer(remainingMs: Long) {
        val minutes = (remainingMs / 1000) / 60
        val seconds = (remainingMs / 1000) % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        
        timerText?.post {
            timerText?.text = "Focus: $timeText"
        }
    }
    
    private fun updatePausedState(paused: Boolean) {
        timerText?.post {
            if (paused) {
                timerText?.text = "Focus: Paused"
            } else {
                val remaining = SessionStateManager.getRemainingTime(this)
                updateTimer(remaining)
            }
        }
    }
    
    private fun removeFloatingView() {
        try {
            floatingView?.let {
                windowManager?.removeView(it)
                floatingView = null
                timerText = null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error removing floating view: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(updateReceiver)
        } catch (e: Exception) {
            // Receiver not registered
        }
        removeFloatingView()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
