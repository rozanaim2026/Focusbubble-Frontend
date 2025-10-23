package com.focusbubble.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusbubble.data.model.Schedule
import com.focusbubble.data.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository
) : ViewModel() {

    private val _schedules = MutableLiveData<List<Schedule>>()
    val schedules: LiveData<List<Schedule>> = _schedules

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage

    /**
     * Fetch all schedules for a user
     */
    fun loadSchedules(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.listSchedules(userId)
                if (response.isSuccessful) {
                    _schedules.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to load schedules: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create a new schedule
     */
    fun createSchedule(
        userId: Int,
        label: String,
        durationMinutes: Int,
        apps: List<String>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.createSchedule(
                    userId = userId,
                    label = label,
                    durationMinutes = durationMinutes,
                    apps = apps
                )
                if (response.isSuccessful) {
                    _successMessage.value = "Schedule created successfully!"
                    // Reload schedules to show the new one
                    loadSchedules(userId)
                } else {
                    _error.value = "Failed to create schedule: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a schedule
     */
    fun deleteSchedule(userId: Int, scheduleId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = repository.deleteSchedule(userId, scheduleId)
                if (response.isSuccessful) {
                    _successMessage.value = "Schedule deleted!"
                    // Reload schedules to reflect the deletion
                    loadSchedules(userId)
                } else {
                    _error.value = "Failed to delete schedule: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
