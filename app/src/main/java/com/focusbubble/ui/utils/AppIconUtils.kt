package com.focusbubble.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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

/** Shared with BlockAppsSheet's icon rendering — used anywhere an app icon needs
 *  to go from a PackageManager Drawable to something Compose can display. */
fun drawableToImageBitmap(drawable: Drawable?): ImageBitmap? {
    drawable ?: return null
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
        drawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap.asImageBitmap()
}
