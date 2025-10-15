package com.focusbubble.ui.utils

import android.content.Context
import android.net.Uri
import com.focusbubble.data.entities.BlockedApp

fun BlockedApp.getIconUri(context: Context): Uri? {
    return try {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        Uri.parse("android.resource://${packageName}/${appInfo.icon}")
    } catch (e: Exception) {
        null
    }
}
