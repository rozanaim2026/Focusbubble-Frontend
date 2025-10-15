package com.focusbubble.utils

import android.content.Context
import android.net.Uri

fun getAppIconUri(context: Context, packageName: String): Uri? {
    return try {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        Uri.parse("android.resource://${packageName}/${appInfo.icon}")
    } catch (e: Exception) {
        null
    }
}
