package com.focusbubble.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.focusbubble.data.entities.BlockedApp;
import com.focusbubble.data.model.BlockedAppCreate;
import com.focusbubble.data.model.BlockedAppResponse;
import com.focusbubble.data.repository.BlockedAppsRepository;
import com.focusbubble.ui.utils.UserAppInfo;
import com.focusbubble.ui.utils.UserSession;
import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.SharingStarted;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0002\b\u0014\n\u0002\u0010\"\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0001\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020\f2\u0006\u0010-\u001a\u00020\f2\u0006\u0010.\u001a\u00020\u0011J\u001c\u0010/\u001a\u00020+2\u0006\u00100\u001a\u00020\u00112\f\u00101\u001a\b\u0012\u0004\u0012\u00020\f0\tJ\u000e\u00102\u001a\u00020+2\u0006\u00103\u001a\u00020\u0019J\u000e\u00104\u001a\u00020+2\u0006\u0010,\u001a\u00020\fJ\u0010\u00105\u001a\u00020\f2\u0006\u00100\u001a\u00020\u0011H\u0002J\u000e\u00106\u001a\u00020+2\u0006\u00100\u001a\u00020\u0011J\u0006\u00107\u001a\u00020+J\u0006\u00108\u001a\u00020+J\u000e\u00109\u001a\u00020+2\u0006\u0010:\u001a\u00020\u0011J\u000e\u0010;\u001a\u00020+2\u0006\u0010<\u001a\u00020\fJ\u000e\u0010=\u001a\u00020+2\u0006\u00100\u001a\u00020\u0011J\"\u0010>\u001a\u00020+2\f\u0010?\u001a\b\u0012\u0004\u0012\u00020\f0@2\f\u0010A\u001a\b\u0012\u0004\u0012\u00020\u001d0\tR\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\f0\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u001d\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\t0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u001d\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u001d0\t0\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010 \u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0016R\u0017\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0016R\u0016\u0010#\u001a\n %*\u0004\u0018\u00010$0$X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00110\u00188F\u00a2\u0006\u0006\u001a\u0004\b\'\u0010\u001bR\u0017\u0010(\u001a\b\u0012\u0004\u0012\u00020\f0\u00188F\u00a2\u0006\u0006\u001a\u0004\b)\u0010\u001b\u00a8\u0006B"}, d2 = {"Lcom/focusbubble/ui/viewmodel/BlockedAppsViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/focusbubble/data/repository/BlockedAppsRepository;", "context", "Landroid/content/Context;", "(Lcom/focusbubble/data/repository/BlockedAppsRepository;Landroid/content/Context;)V", "_activeBlocksFromBackend", "Landroidx/lifecycle/MutableLiveData;", "", "Lcom/focusbubble/data/model/BlockedAppResponse;", "_error", "", "_isLoading", "", "_selectedDurationMinutes", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_selectedQuote", "activeBlocksFromBackend", "Landroidx/lifecycle/LiveData;", "getActiveBlocksFromBackend", "()Landroidx/lifecycle/LiveData;", "blockedApps", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/focusbubble/data/entities/BlockedApp;", "getBlockedApps", "()Lkotlinx/coroutines/flow/StateFlow;", "blockedAppsUi", "Lcom/focusbubble/ui/utils/UserAppInfo;", "getBlockedAppsUi", "currentUserId", "error", "getError", "isLoading", "prefs", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "selectedDurationMinutes", "getSelectedDurationMinutes", "selectedQuote", "getSelectedQuote", "addApp", "", "packageName", "appName", "duration", "createBlocksOnBackend", "userId", "packageNames", "deleteApp", "app", "deleteAppByPackageName", "durationPrefKey", "fetchActiveBlocksFromBackend", "refreshBackendBlocks", "refreshForCurrentUser", "setSelectedDuration", "minutes", "setSelectedQuote", "quote", "syncBlocksFromBackend", "updateBlockedApps", "selectedPackages", "", "allApps", "app_debug"})
@kotlin.OptIn(markerClass = {kotlinx.coroutines.ExperimentalCoroutinesApi.class})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class BlockedAppsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.focusbubble.data.repository.BlockedAppsRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> currentUserId = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.focusbubble.data.entities.BlockedApp>> blockedApps = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.focusbubble.ui.utils.UserAppInfo>> blockedAppsUi = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _selectedDurationMinutes = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _selectedQuote = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.focusbubble.data.model.BlockedAppResponse>> _activeBlocksFromBackend = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.util.List<com.focusbubble.data.model.BlockedAppResponse>> activeBlocksFromBackend = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.String> _error = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.String> error = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.MutableLiveData<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.lifecycle.LiveData<java.lang.Boolean> isLoading = null;
    
    @javax.inject.Inject()
    public BlockedAppsViewModel(@org.jetbrains.annotations.NotNull()
    com.focusbubble.data.repository.BlockedAppsRepository repository, @dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Call this right after login (any path) and right after logout —
     * see the "dashboard" composable in MainActivity.kt.
     */
    public final void refreshForCurrentUser() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.focusbubble.data.entities.BlockedApp>> getBlockedApps() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.focusbubble.ui.utils.UserAppInfo>> getBlockedAppsUi() {
        return null;
    }
    
    private final java.lang.String durationPrefKey(int userId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getSelectedDurationMinutes() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getSelectedQuote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.util.List<com.focusbubble.data.model.BlockedAppResponse>> getActiveBlocksFromBackend() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.String> getError() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final androidx.lifecycle.LiveData<java.lang.Boolean> isLoading() {
        return null;
    }
    
    public final void setSelectedDuration(int minutes) {
    }
    
    public final void setSelectedQuote(@org.jetbrains.annotations.NotNull()
    java.lang.String quote) {
    }
    
    public final void addApp(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName, @org.jetbrains.annotations.NotNull()
    java.lang.String appName, int duration) {
    }
    
    public final void deleteApp(@org.jetbrains.annotations.NotNull()
    com.focusbubble.data.entities.BlockedApp app) {
    }
    
    public final void deleteAppByPackageName(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
    }
    
    public final void updateBlockedApps(@org.jetbrains.annotations.NotNull()
    java.util.Set<java.lang.String> selectedPackages, @org.jetbrains.annotations.NotNull()
    java.util.List<com.focusbubble.ui.utils.UserAppInfo> allApps) {
    }
    
    /**
     * Fetch active blocks from backend for a user
     * This shows what's currently blocked on the server
     */
    public final void fetchActiveBlocksFromBackend(int userId) {
    }
    
    /**
     * Sync backend blocks to local database
     * Call this after starting a session
     */
    public final void syncBlocksFromBackend(int userId) {
    }
    
    /**
     * Create blocks on backend (usually done when starting a session)
     */
    public final void createBlocksOnBackend(int userId, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> packageNames) {
    }
    
    /**
     * Manually refresh/expire blocks on backend
     */
    public final void refreshBackendBlocks() {
    }
}