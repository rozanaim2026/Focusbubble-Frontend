package com.focusbubble.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusbubble.data.entities.BlockedApp
import com.focusbubble.data.model.BlockedAppCreate
import com.focusbubble.data.model.BlockedAppResponse
import com.focusbubble.data.repository.BlockedAppsRepository
import com.focusbubble.ui.utils.UserAppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockedAppsViewModel @Inject constructor(
    private val repository: BlockedAppsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val prefs = context.getSharedPreferences("FocusBubblePrefs", Context.MODE_PRIVATE)

    // ========== EXISTING LOCAL DATABASE STATE ==========

    val blockedApps: StateFlow<List<BlockedApp>> = repository.blockedApps
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _blockedAppsUi = MutableStateFlow<List<UserAppInfo>>(emptyList())
    val blockedAppsUi: StateFlow<List<UserAppInfo>> get() = _blockedAppsUi

    // Load saved duration from SharedPreferences (default 25 if not set)
    private val _selectedDurationMinutes = MutableStateFlow(
        prefs.getInt("selected_duration", 25)
    )
    val selectedDurationMinutes: StateFlow<Int> get() = _selectedDurationMinutes

    private val _selectedQuote = MutableStateFlow("Motivational")
    val selectedQuote: StateFlow<String> get() = _selectedQuote
    
    init {
        // Load blocked apps from database on initialization
        viewModelScope.launch {
            blockedApps.collect { apps ->
                // Keep UI list in sync with database
                android.util.Log.d("BlockedAppsViewModel", "Loaded ${apps.size} blocked apps from DB")
            }
        }
    }

    // ========== NEW BACKEND STATE ==========

    private val _activeBlocksFromBackend = MutableLiveData<List<BlockedAppResponse>>()
    val activeBlocksFromBackend: LiveData<List<BlockedAppResponse>> = _activeBlocksFromBackend

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // ========== EXISTING LOCAL METHODS ==========

    fun setSelectedDuration(minutes: Int) {
        _selectedDurationMinutes.value = minutes
        // Save to SharedPreferences so it persists
        prefs.edit().putInt("selected_duration", minutes).apply()
    }

    fun setSelectedQuote(quote: String) {
        _selectedQuote.value = quote
    }

    fun addApp(packageName: String, appName: String, duration: Int) {
        viewModelScope.launch {
            repository.addBlockedApp(
                BlockedApp(
                    packageName = packageName,
                    appName = appName,
                    durationMinutes = duration
                )
            )
        }
    }

    fun deleteApp(app: BlockedApp) {
        viewModelScope.launch {
            repository.deleteBlockedApp(app)
        }
    }

    fun deleteAppByPackageName(packageName: String) {
        viewModelScope.launch {
            blockedApps.value.find { it.packageName == packageName }?.let {
                repository.deleteBlockedApp(it)
            }
        }
    }

    fun updateBlockedApps(selectedPackages: Set<String>, allApps: List<UserAppInfo>) {
        viewModelScope.launch {
            android.util.Log.d("BlockedAppsViewModel", "Updating blocked apps - Selected: ${selectedPackages.size}, Current DB: ${blockedApps.value.size}")
            
            // FIRST: Delete ALL existing blocked apps to prevent duplicates
            val toDelete = blockedApps.value
            toDelete.forEach {
                android.util.Log.d("BlockedAppsViewModel", "Deleting: ${it.appName} (${it.packageName})")
                deleteApp(it)
            }
            
            // THEN: Add only the newly selected apps
            selectedPackages.forEach { pkg ->
                allApps.find { it.packageName == pkg }?.let {
                    android.util.Log.d("BlockedAppsViewModel", "Adding: ${it.appName} ($pkg)")
                    addApp(pkg, it.appName, 30)
                }
            }

            // Update UI list
            _blockedAppsUi.value = allApps.filter { it.packageName in selectedPackages }
            
            android.util.Log.d("BlockedAppsViewModel", "Update complete - Now blocking ${selectedPackages.size} apps")
        }
    }

    // ========== NEW BACKEND METHODS ==========

    /**
     * Fetch active blocks from backend for a user
     * This shows what's currently blocked on the server
     */
    fun fetchActiveBlocksFromBackend(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getActiveBlocksFromBackend(userId)
                if (response.isSuccessful) {
                    _activeBlocksFromBackend.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Failed to fetch blocks: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sync backend blocks to local database
     * Call this after starting a session
     */
    fun syncBlocksFromBackend(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.syncFromBackend(userId)
                // Local blockedApps Flow will automatically update
            } catch (e: Exception) {
                _error.value = "Sync error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create blocks on backend (usually done when starting a session)
     */
    fun createBlocksOnBackend(userId: Int, packageNames: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blocksToCreate = packageNames.map { pkg ->
                    BlockedAppCreate(
                        packageName = pkg,
                        appName = blockedApps.value.find { it.packageName == pkg }?.appName
                    )
                }

                val response = repository.createBlocksOnBackend(userId, blocksToCreate)
                if (response.isSuccessful) {
                    // Optionally sync back to local DB
                    syncBlocksFromBackend(userId)
                } else {
                    _error.value = "Failed to create blocks: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Manually refresh/expire blocks on backend
     */
    fun refreshBackendBlocks() {
        viewModelScope.launch {
            try {
                val response = repository.refreshBlocksOnBackend()
                if (response.isSuccessful) {
                    val expired = response.body()?.expired ?: 0
                    // Optionally show a message about expired blocks
                } else {
                    _error.value = "Failed to refresh blocks: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }
}