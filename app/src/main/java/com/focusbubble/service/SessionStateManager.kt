package com.focusbubble.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log

object SessionStateManager {

    private const val TAG =
        "SessionStateManager"

    private const val PREFS_NAME =
        "focus_session_state"

    private const val CELEBRATION_PREFS_NAME =
        "focus_celebration_state"

    private const val KEY_IS_PAUSED =
        "is_paused"

    private const val KEY_REMAINING_TIME =
        "remaining_time_ms"

    private const val KEY_SESSION_ID =
        "session_id"

    private const val KEY_SESSION_ACTIVE =
        "session_active"

    private const val KEY_TOTAL_DURATION =
        "total_duration_ms"

    private const val KEY_SESSION_END_TIME =
        "session_end_time_ms"

    private const val KEY_APP_FOREGROUND =
        "app_foreground"

    private const val KEY_PENDING_CELEBRATION =
        "pending_celebration"

    private const val KEY_COMPLETED_SECONDS =
        "completed_seconds"

    const val ACTION_SESSION_PAUSED =
        "com.focusbubble.SESSION_PAUSED"

    const val ACTION_SESSION_RESUMED =
        "com.focusbubble.SESSION_RESUMED"

    const val ACTION_SESSION_STOPPED =
        "com.focusbubble.SESSION_STOPPED"

    const val ACTION_SESSION_FINISHED =
        "com.focusbubble.SESSION_FINISHED"

    const val ACTION_UPDATE_TIMER =
        "com.focusbubble.UPDATE_TIMER"

    const val ACTION_APP_FOREGROUND =
        "com.focusbubble.APP_FOREGROUND"

    const val ACTION_APP_BACKGROUND =
        "com.focusbubble.APP_BACKGROUND"

    const val ACTION_BLOCK_DISMISSED =
        "com.focusbubble.BLOCK_DISMISSED"

    const val EXTRA_REMAINING_TIME =
        "remaining_time"

    const val ACTION_LEAVE_TO_HOME =
        "com.focusbubble.LEAVE_TO_HOME"

    private fun getPrefs(
        context: Context
    ): SharedPreferences {
        return context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    private fun getCelebrationPrefs(
        context: Context
    ): SharedPreferences {
        return context.getSharedPreferences(
            CELEBRATION_PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    private fun sendAppBroadcast(
        context: Context,
        action: String,
        extras: Intent.() -> Unit = {}
    ) {
        val intent =
            Intent(action).apply {
                setPackage(context.packageName)
                extras()
            }

        context.sendBroadcast(intent)
    }

    fun resetPausedFlag(
        context: Context
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_IS_PAUSED,
                false
            )
            .apply()
    }

    fun isSessionActive(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_SESSION_ACTIVE,
                false
            )
    }

    fun setSessionActive(
        context: Context,
        active: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_SESSION_ACTIVE,
                active
            )
            .apply()
    }

    fun isPaused(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_IS_PAUSED,
                false
            )
    }

    fun pauseSession(
        context: Context
    ) {
        if (!isSessionActive(context)) {
            Log.d(
                TAG,
                "Pause ignored; no active session"
            )
            return
        }

        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_IS_PAUSED,
                true
            )
            .apply()

        sendAppBroadcast(
            context,
            ACTION_SESSION_PAUSED
        )
    }

    fun resumeSession(
        context: Context
    ) {
        val remaining =
            getRemainingTime(context)

        if (remaining <= 0L) {
            Log.w(
                TAG,
                "Resume ignored; remaining=$remaining"
            )
            return
        }

        setSessionEndTime(
            context,
            System.currentTimeMillis() + remaining
        )

        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_IS_PAUSED,
                false
            )
            .putBoolean(
                KEY_SESSION_ACTIVE,
                true
            )
            .apply()

        Log.d(
            TAG,
            "▶️ Session resumed; remaining=$remaining"
        )

        sendAppBroadcast(
            context,
            ACTION_SESSION_RESUMED
        )
    }

    fun getRemainingTime(
        context: Context
    ): Long {
        val endTime =
            getSessionEndTime(context)

        if (
            endTime > 0L &&
            !isPaused(context)
        ) {
            return (
                    endTime -
                            System.currentTimeMillis()
                    )
                .coerceAtLeast(0L)
        }

        return getPrefs(context)
            .getLong(
                KEY_REMAINING_TIME,
                0L
            )
    }

    fun setRemainingTime(
        context: Context,
        timeMs: Long
    ) {
        val safeTime =
            timeMs.coerceAtLeast(0L)

        getPrefs(context)
            .edit()
            .putLong(
                KEY_REMAINING_TIME,
                safeTime
            )
            .apply()

        sendAppBroadcast(
            context,
            ACTION_UPDATE_TIMER
        ) {
            putExtra(
                EXTRA_REMAINING_TIME,
                safeTime
            )
        }
    }

    fun getTotalDuration(
        context: Context
    ): Long {
        return getPrefs(context)
            .getLong(
                KEY_TOTAL_DURATION,
                25L * 60L * 1000L
            )
    }

    fun setTotalDuration(
        context: Context,
        durationMs: Long
    ) {
        getPrefs(context)
            .edit()
            .putLong(
                KEY_TOTAL_DURATION,
                durationMs
            )
            .apply()
    }

    fun getSessionEndTime(
        context: Context
    ): Long {
        return getPrefs(context)
            .getLong(
                KEY_SESSION_END_TIME,
                0L
            )
    }

    fun setSessionEndTime(
        context: Context,
        endTimeMillis: Long
    ) {
        getPrefs(context)
            .edit()
            .putLong(
                KEY_SESSION_END_TIME,
                endTimeMillis
            )
            .apply()
    }

    fun isAppForeground(
        context: Context
    ): Boolean {
        return getPrefs(context)
            .getBoolean(
                KEY_APP_FOREGROUND,
                true
            )
    }

    fun setAppForeground(
        context: Context,
        isForeground: Boolean
    ) {
        getPrefs(context)
            .edit()
            .putBoolean(
                KEY_APP_FOREGROUND,
                isForeground
            )
            .apply()

        val action =
            if (isForeground) {
                ACTION_APP_FOREGROUND
            } else {
                ACTION_APP_BACKGROUND
            }

        sendAppBroadcast(
            context,
            action
        )
    }

    fun setPendingCelebration(
        context: Context,
        completedSeconds: Int
    ) {
        getCelebrationPrefs(context)
            .edit()
            .putBoolean(
                KEY_PENDING_CELEBRATION,
                true
            )
            .putInt(
                KEY_COMPLETED_SECONDS,
                completedSeconds
            )
            .apply()
    }

    fun hasPendingCelebration(
        context: Context
    ): Boolean {
        return getCelebrationPrefs(context)
            .getBoolean(
                KEY_PENDING_CELEBRATION,
                false
            )
    }

    fun getPendingCelebrationSeconds(
        context: Context
    ): Int {
        return getCelebrationPrefs(context)
            .getInt(
                KEY_COMPLETED_SECONDS,
                0
            )
    }

    fun clearPendingCelebration(
        context: Context
    ) {
        getCelebrationPrefs(context)
            .edit()
            .putBoolean(
                KEY_PENDING_CELEBRATION,
                false
            )
            .apply()
    }

    fun getSessionId(
        context: Context
    ): Int {
        return getPrefs(context)
            .getInt(
                KEY_SESSION_ID,
                -1
            )
    }

    fun setSessionId(
        context: Context,
        sessionId: Int
    ) {
        getPrefs(context)
            .edit()
            .putInt(
                KEY_SESSION_ID,
                sessionId
            )
            .apply()
    }

    fun clearSession(
        context: Context
    ) {
        getPrefs(context)
            .edit()
            .clear()
            .apply()

        sendAppBroadcast(
            context,
            ACTION_SESSION_STOPPED
        )
    }
}