package com.focusbubble.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focusbubble.service.WeeklyStatsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FocusStatsViewModel(application: Application) : AndroidViewModel(application) {

    private val _weeklyFocusTime = MutableStateFlow(0) // in seconds
    val weeklyFocusTime: StateFlow<Int> = _weeklyFocusTime

    init {
        refresh()
    }

    fun addFocusTime(seconds: Int) {
        viewModelScope.launch {
            WeeklyStatsManager.creditFocusTime(getApplication(), seconds)
            refresh()
        }
    }

    fun refresh() {
        _weeklyFocusTime.value = WeeklyStatsManager.getWeeklySeconds(getApplication())
        android.util.Log.d(
            "FocusStatsViewModel",
            "refresh() on instance@${System.identityHashCode(this)} -> ${_weeklyFocusTime.value}s"
        )
    }
}