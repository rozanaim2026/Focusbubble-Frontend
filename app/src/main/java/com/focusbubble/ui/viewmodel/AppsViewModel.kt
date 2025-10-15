package com.focusbubble.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusbubble.ui.utils.UserAppInfo
import com.focusbubble.utils.getAppIconUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppsViewModel(application: Application) : AndroidViewModel(application) {

    private val _apps = MutableStateFlow<List<UserAppInfo>>(emptyList())
    val apps: StateFlow<List<UserAppInfo>> = _apps

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val installedApps = pm.getInstalledApplications(0)
            val list = installedApps.map { app ->
                val iconUri = getAppIconUri(getApplication(), app.packageName)?.toString()
                UserAppInfo(
                    appName = pm.getApplicationLabel(app).toString(),
                    packageName = app.packageName,
                    iconUri = iconUri,
                    durationMinutes = 30
                )
            }.sortedBy { it.appName.lowercase() }
            _apps.value = list
        }
    }
}
