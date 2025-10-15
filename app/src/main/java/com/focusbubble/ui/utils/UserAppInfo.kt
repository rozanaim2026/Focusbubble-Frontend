package com.focusbubble.ui.utils

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap

data class UserAppInfo(
    val packageName: String,
    val appName: String,
    val drawable: Drawable? = null,   // original icon drawable
    val iconBitmap: ImageBitmap? = null,// optional converted bitmap
    val iconUri: String? = null,   // For Coil
    val durationMinutes: Int = 30
)
