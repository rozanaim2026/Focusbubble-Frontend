package com.focusbubble.service

import android.content.Context
import android.content.SharedPreferences

/**
 * Singleton to manage focus session state across services and UI
 * Handles pause/resume state, remaining time, and session ID
 */
object SessionStateManager {
    
    private const val PREFS_NAME = "focus_session_state"
    private const val KEY_IS_PAUSED = "is_paused"
    private const val KEY_REMAINING_TIME = "remaining_time_ms"
    private const val KEY_SESSION_ID = "session_id"
    private const val KEY_SESSION_ACTIVE = "session_active"
    private const val KEY_TOTAL_DURATION = "total_duration_ms"
    
    // Broadcast actions
    const val ACTION_SESSION_PAUSED = "com.focusbubble.SESSION_PAUSED"
    const val ACTION_SESSION_RESUMED = "com.focusbubble.SESSION_RESUMED"
    const val ACTION_SESSION_STOPPED = "com.focusbubble.SESSION_STOPPED"
    const val ACTION_UPDATE_TIMER = "com.focusbubble.UPDATE_TIMER"
    const val EXTRA_REMAINING_TIME = "remaining_time"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun isSessionActive(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SESSION_ACTIVE, false)
    }
    
    fun setSessionActive(context: Context, active: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SESSION_ACTIVE, active).apply()
    }
    
    fun isPaused(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_PAUSED, false)
    }
    
    fun pauseSession(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_IS_PAUSED, true).apply()
        // Send broadcast so UI can update
        val intent = android.content.Intent(ACTION_SESSION_PAUSED)
        context.sendBroadcast(intent)
    }
    
    fun resumeSession(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_IS_PAUSED, false).apply()
        // Send broadcast so UI can update
        val intent = android.content.Intent(ACTION_SESSION_RESUMED)
        context.sendBroadcast(intent)
    }
    
    fun getRemainingTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_REMAINING_TIME, 0L)
    }
    
    fun setRemainingTime(context: Context, timeMs: Long) {
        getPrefs(context).edit().putLong(KEY_REMAINING_TIME, timeMs).apply()
        // Send broadcast to update floating timer
        val intent = android.content.Intent(ACTION_UPDATE_TIMER)
        intent.putExtra(EXTRA_REMAINING_TIME, timeMs)
        context.sendBroadcast(intent)
    }
    
    fun getTotalDuration(context: Context): Long {
        return getPrefs(context).getLong(KEY_TOTAL_DURATION, 25 * 60 * 1000L)
    }
    
    fun setTotalDuration(context: Context, durationMs: Long) {
        getPrefs(context).edit().putLong(KEY_TOTAL_DURATION, durationMs).apply()
    }
    
    fun getSessionId(context: Context): Int {
        return getPrefs(context).getInt(KEY_SESSION_ID, -1)
    }
    
    fun setSessionId(context: Context, sessionId: Int) {
        getPrefs(context).edit().putInt(KEY_SESSION_ID, sessionId).apply()
    }
    
    fun clearSession(context: Context) {
        getPrefs(context).edit().clear().apply()
        val intent = android.content.Intent(ACTION_SESSION_STOPPED)
        context.sendBroadcast(intent)
    }
}
