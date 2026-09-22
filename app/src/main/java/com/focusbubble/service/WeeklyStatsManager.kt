package com.focusbubble.service

import android.content.Context
import android.util.Log
import com.focusbubble.ui.utils.UserSession
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields

/**
 * Every key here is scoped by the CURRENT logged-in user's id
 * (UserSession.getUserId). Previously "current_week_key" and
 * "weekly_time_seconds" were bare, unscoped keys shared by literally every
 * user of the app on the device — whichever user completed a session most
 * recently, every OTHER user (a different Gmail account, a different guest)
 * would see that same total. Scoping every key by userId gives each user
 * their own independent slot in the same file, with zero cross-contamination.
 */
object WeeklyStatsManager {
    private const val PREFS_NAME = "focus_stats"

    fun currentWeekKey(): String {
        val today = LocalDate.now()
        val weekFields = WeekFields.ISO
        val week = today.get(weekFields.weekOfWeekBasedYear())
        val weekYear = today.get(weekFields.weekBasedYear())
        return "$weekYear-W$week"
    }

    private fun weekKeyPrefKey(userId: Int) = "current_week_key_$userId"
    private fun secondsPrefKey(userId: Int) = "weekly_time_seconds_$userId"

    fun creditFocusTime(context: Context, seconds: Int) {
        if (seconds <= 0) {
            Log.d("WeeklyStatsManager", "creditFocusTime called with seconds=$seconds, skipping (not positive)")
            return
        }

        val userId = UserSession.getUserId(context)
        if (userId == -1) {
            Log.w("WeeklyStatsManager", "creditFocusTime called with no logged-in user — skipping rather than crediting a shared bucket")
            return
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val nowKey = currentWeekKey()
        val savedKey = prefs.getString(weekKeyPrefKey(userId), null)
        val currentTotal = if (savedKey == nowKey) prefs.getInt(secondsPrefKey(userId), 0) else 0
        val newTotal = currentTotal + seconds

        prefs.edit()
            .putString(weekKeyPrefKey(userId), nowKey)
            .putInt(secondsPrefKey(userId), newTotal)
            .apply()

        Log.d("WeeklyStatsManager", "creditFocusTime[user=$userId]: +$seconds sec | $currentTotal -> $newTotal")
    }

    fun getWeeklySeconds(context: Context): Int {
        val userId = UserSession.getUserId(context)
        if (userId == -1) {
            Log.w("WeeklyStatsManager", "getWeeklySeconds called with no logged-in user — returning 0")
            return 0
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedKey = prefs.getString(weekKeyPrefKey(userId), null)
        val nowKey = currentWeekKey()
        val result = if (savedKey == nowKey) prefs.getInt(secondsPrefKey(userId), 0) else 0
        Log.d("WeeklyStatsManager", "getWeeklySeconds[user=$userId]: -> $result")
        return result
    }

    fun clearForUser(context: Context, userId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(weekKeyPrefKey(userId))
            .remove(secondsPrefKey(userId))
            .apply()
    }
}