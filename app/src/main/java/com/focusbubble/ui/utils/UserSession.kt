package com.focusbubble.ui.utils

import android.content.Context
import java.util.UUID

object UserSession {
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_GUEST_EMAIL = "guest_email"

    fun saveUser(context: Context, userId: Int, email: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_USER_ID, -1)
    }

    fun getUserEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun isLoggedIn(context: Context): Boolean = getUserId(context) != -1

    /**
     * Removes ONLY the "who is currently logged in" markers — deliberately NOT
     * a blanket .clear(), which would also wipe KEY_GUEST_EMAIL below and
     * silently break guest identity continuity. Logout must never delete data;
     * it only ends the current session.
     */
    fun clearUser(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .apply()
    }

    /**
     * A stable, device-local identity for "Skip and Continue" users. Generated
     * ONCE and reused on every subsequent Skip tap, so the backend's
     * get_or_create_user() (keyed by email) returns the SAME user row every
     * time instead of creating a brand-new guest identity on every tap.
     */
    fun getOrCreateGuestEmail(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_GUEST_EMAIL, null)
        if (existing != null) return existing
        val newGuestEmail = "guest_${UUID.randomUUID()}@focusbubble.app"
        prefs.edit().putString(KEY_GUEST_EMAIL, newGuestEmail).apply()
        return newGuestEmail
    }

    /** Only for account deletion — an actual new guest identity should be
     *  possible after a guest deletes their account. */
    fun clearGuestEmail(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_GUEST_EMAIL).apply()
    }
}