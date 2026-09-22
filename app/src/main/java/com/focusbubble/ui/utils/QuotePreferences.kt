package com.focusbubble.ui.utils

import android.content.Context

object QuotePreferences {
    private const val PREFS_NAME = "FocusBubblePrefs"
    private const val KEY_PREFIX = "selected_quote_categories_"

    private fun key(context: Context) = KEY_PREFIX + UserSession.getUserId(context)

    fun getSelectedCategories(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(key(context), emptySet()) ?: emptySet()
    }

    fun setSelectedCategories(context: Context, categories: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(key(context), categories).apply()
    }

    fun clearForUser(context: Context, userId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PREFIX + userId).apply()
    }
}