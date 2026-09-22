package com.focusbubble

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.focusbubble.service.SessionStateManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FocusBubbleApplication : Application() {

    private var startedActivityCount = 0

    override fun onCreate() {
        super.onCreate()

        com.jakewharton.threetenabp.AndroidThreeTen.init(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                startedActivityCount++
                if (startedActivityCount == 1) {
                    SessionStateManager.setAppForeground(this@FocusBubbleApplication, true)
                    sendBroadcast(Intent(SessionStateManager.ACTION_APP_FOREGROUND))
                }
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivityCount--
                if (startedActivityCount <= 0) {
                    startedActivityCount = 0
                    SessionStateManager.setAppForeground(this@FocusBubbleApplication, false)
                    sendBroadcast(Intent(SessionStateManager.ACTION_APP_BACKGROUND))
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
