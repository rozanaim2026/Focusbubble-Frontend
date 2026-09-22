package com.focusbubble

import android.content.ComponentName
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.util.Log
import com.focusbubble.data.AppDatabase
import com.focusbubble.service.SessionStateManager
import com.focusbubble.ui.utils.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FocusBubbleNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "FocusBubbleMediaListener"

        // Set by FocusBubbleAccessibilityService the instant it blocks an app —
        // this service reacts to that signal instead of independently polling,
        // so media gets paused at the same moment the overlay appears.
        @Volatile
        var instance: FocusBubbleNotificationListener? = null
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "Notification listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "Notification listener disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }

    /**
     * Called by FocusBubbleAccessibilityService right after it detects and
     * blocks an app. Finds that app's active media session (if any) and
     * pauses it — this is what actually stops YouTube/Spotify/etc. audio or
     * video from continuing to play once the block screen is up, since
     * sending the app "home" or covering it with an overlay alone has no
     * effect on a media session running independently in the background.
     */
    fun pauseMediaFor(packageName: String) {
        serviceScope.launch {
            try {
                val mediaSessionManager =
                    getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager

                val componentName = ComponentName(this@FocusBubbleNotificationListener, FocusBubbleNotificationListener::class.java)
                val controllers: List<MediaController> =
                    mediaSessionManager.getActiveSessions(componentName)

                val target = controllers.firstOrNull { it.packageName == packageName }
                if (target != null) {
                    target.transportControls.pause()
                    Log.d(TAG, "⏸️ Paused media session for: $packageName")
                } else {
                    Log.d(TAG, "No active media session found for: $packageName")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "No notification access permission — cannot pause media", e)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to pause media for $packageName: ${e.message}", e)
            }
        }
    }

    // Required override — we don't need to act on individual notification
    // postings, only need the binding this service provides for
    // getActiveSessions() to work at all.
    override fun onNotificationPosted(sbn: android.service.notification.StatusBarNotification?) {}
    override fun onNotificationRemoved(sbn: android.service.notification.StatusBarNotification?) {}
}