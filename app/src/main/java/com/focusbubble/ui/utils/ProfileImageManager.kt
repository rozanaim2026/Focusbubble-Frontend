package com.focusbubble.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Saves and loads the user's profile picture to local app storage.
 * No network/backend involved — picture stays on-device.
 */
object ProfileImageManager {

    private const val FILE_NAME = "profile_picture.jpg"

    fun saveProfileImage(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val file = File(context.filesDir, FILE_NAME)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("ProfileImageManager", "Failed to save profile image: ${e.message}")
            false
        }
    }

    fun loadProfileImage(context: Context): Bitmap? {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            android.util.Log.e("ProfileImageManager", "Failed to load profile image: ${e.message}")
            null
        }
    }

    fun hasProfileImage(context: Context): Boolean {
        return File(context.filesDir, FILE_NAME).exists()
    }
}
