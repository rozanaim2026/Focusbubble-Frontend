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
import com.focusbubble.ui.utils.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BlockedAppsViewModel @Inject constructor(
    private val repository: BlockedAppsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("FocusBubblePrefs", Context.MODE_PRIVATE)

    // ========== USER ISOLATION ==========
    // This ViewModel is a singleton for the app's whole lifetime (scoped to
    // the Activity, not recreated on login/logout) — so it can genuinely span
    // multiple different users across a single process if someone logs out
    // and a different person logs in. currentUserId is what makes every
    // downstream Flow re-point at the NEW user's data the moment
    // refreshForCurrentUser() is called, without needing this ViewModel
    // itself to be recreated.
    private val currentUserId = MutableStateFlow(UserSession.getUserId(context))

    /** Call this right after login (any path) and right after logout —
     *  see the "dashboard" composable in MainActivity.kt. */
    fun refreshForCurrentUser() {
        val userId = UserSession.getUserId(context)
        currentUserId.value = userId
        _selectedDurationMinutes.value = prefs.getInt(durationPrefKey(userId), 25)
    }

    // ========== EXISTING LOCAL DATABASE STATE ==========

    val blockedApps: StateFlow<List<BlockedApp>> = currentUserId
        .flatMapLatest { userId -> repository.blockedAppsForUser(userId) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    // Derived directly from the persisted DB — always reflects what's actually
    // saved, correctly surviving app restarts, navigation, and session completion,
    // instead of only existing while the selection sheet happens to be open.
    val blockedAppsUi: StateFlow<List<UserAppInfo>> = blockedApps
        .map { entities ->
            entities.map { entity ->
                val icon = try {
                    context.packageManager.getApplicationIcon(entity.packageName)
                } catch (e: Exception) {
                    null
                }
                UserAppInfo(
                    packageName = entity.packageName,
                    appName = entity.appName,
                    drawable = icon,
                    iconBitmap = com.focusbubble.ui.utils.drawableToImageBitmap(icon),
                    durationMinutes = entity.durationMinutes
                )
            }
        }
        .flowOn(kotlinx.coroutines.Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Load saved duration from SharedPreferences (default 25 if not set)
    private fun durationPrefKey(userId: Int) = "selected_duration_$userId"

    // Load saved duration from SharedPreferences (default 25 if not set) —
    // scoped by user, same reasoning as blockedApps above.
    private val _selectedDurationMinutes = MutableStateFlow(
        prefs.getInt(durationPrefKey(UserSession.getUserId(context)), 25)
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
        prefs.edit().putInt(durationPrefKey(UserSession.getUserId(context)), minutes).apply()
    }

    fun setSelectedQuote(quote: String) {
        _selectedQuote.value = quote
    }

    fun addApp(packageName: String, appName: String, duration: Int) {
        viewModelScope.launch {
            repository.addBlockedApp(
                BlockedApp(
                    userId = UserSession.getUserId(context),
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

            val userId = com.focusbubble.ui.utils.UserSession.getUserId(context)

            // One atomic replace instead of N deletes + N inserts as separate
            // writes — see the matching comment on BlockedAppDao.replaceAllForUser
            // for why this is what actually fixes the "blinks twice" glitch.
            val newEntities = selectedPackages.mapNotNull { pkg ->
                allApps.find { it.packageName == pkg }?.let {
                    BlockedApp(
                        userId = userId,
                        packageName = pkg,
                        appName = it.appName,
                        durationMinutes = 30
                    )
                }
            }
            repository.replaceAllForUser(userId, newEntities)

            // Also push the selection to the backend, scoped to this exact user.
            if (userId != -1 && selectedPackages.isNotEmpty()) {
                createBlocksOnBackend(userId, selectedPackages.toList())
            }

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