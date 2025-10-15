package com.focusbubble.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.focusbubble.data.entities.BlockedApp;
import com.focusbubble.data.model.BlockedAppCreate;
import com.focusbubble.data.model.BlockedAppResponse;
import com.focusbubble.data.repository.BlockedAppsRepository;
import com.focusbubble.ui.utils.UserAppInfo;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import kotlinx.coroutines.flow.SharingStarted;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u0002\n\u0002\b\u0012\n\u0002\u0010\"\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020\r2\u0006\u0010(\u001a\u00020\r2\u0006\u0010)\u001a\u00020\u0011J\u001c\u0010*\u001a\u00020&2\u0006\u0010+\u001a\u00020\u00112\f\u0010,\u001a\b\u0012\u0004\u0012\u00020\r0\u0007J\u000e\u0010-\u001a\u00020&2\u0006\u0010.\u001a\u00020\u0019J\u000e\u0010/\u001a\u00020&2\u0006\u0010\'\u001a\u00020\rJ\u000e\u00100\u001a\u00020&2\u0006\u0010+\u001a\u00020\u0011J\u0006\u00101\u001a\u00020&J\u000e\u00102\u001a\u00020&2\u0006\u00103\u001a\u00020\u0011J\u000e\u00104\u001a\u00020&2\u0006\u00105\u001a\u00020\rJ\u000e\u00106\u001a\u00020&2\u0006\u0010+\u001a\u00020\u0011J\"\u00107\u001a\u00020&2\f\u00108\u001a\b\u0012\u0004\u0012\u00020\r092\f\u0010:\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0007R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u00070\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\r0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u001d\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u00070\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u001d\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\u00070\u00188F\u00a2\u0006\u0006\u001a\u0004\b\u001d\u0010\u001bR\u0019\u0010\u001e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0016R\u0017\u0010 \u001a\b\u0012\u0004\u0012\u00020\u000f0\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00110\u00188F\u00a2\u0006\u0006\u001a\u0004\b\"\u0010\u001bR\u0017\u0010#\u001a\b\u0012\u0004\u0012\u00020\r0\u00188F\u00a2\u0006\u0006\u001a\u0004\b$\u0010\u001b\u00a8\u0006;"}, d2 = {"Lcom/focusbubble/ui/viewmodel/BlockedAppsViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/focusbubble/data/repository/BlockedAppsRepository;", "(Lcom/focusbubble/data/repository/BlockedAppsRepository;)V", "_activeBlocksFromBackend", "Landroidx/lifecycle/MutableLiveData;", "", "Lcom/focusbubble/data/model/BlockedAppResponse;", "_blockedAppsUi", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/focusbubble/ui/utils/UserAppInfo;", "_error", "", "_isLoading", "", "_selectedDurationMinutes", "", "_selectedQuote", "activeBlocksFromBackend", "Landroidx/lifecycle/LiveData;", "getActiveBlocksFromBackend", "()Landroidx/lifecycle/LiveData;", "blockedApps", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/focusbubble/data/entities/BlockedApp;", "getBlockedApps", "()Lkotlinx/coroutines/flow/StateFlow;", "blockedAppsUi", "getBlockedAppsUi", "error", "getError", "isLoading", "selectedDurationMinutes", "getSelectedDurationMinutes", "selectedQuote", "getSelectedQuote", "addApp", "", "packageName", "appName", "duration", "createBlocksOnBackend", "userId", "packageNames", "deleteApp", "app", "deleteAppByPackageName", "fetchActiveBlocksFromBackend", "refreshBackendBlocks", "setSelectedDuration", "minutes", "setSelectedQuote", "quote", "syncBlocksFromBackend", "updateBlockedApps", "selectedPackages", "", "allApps", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class BlockedAppsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.focusbubble.data.repository.BlockedAppsRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.focusbubble.data.entities.BlockedApp>> blockedApps = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.focusbubble.ui.utils.UserAppInfo>> _blockedAppsUi = null;
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
    com.focusbubble.data.repository.BlockedAppsRepository repository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.focusbubble.data.entities.BlockedApp>> getBlockedApps() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.focusbubble.ui.utils.UserAppInfo>> getBlockedAppsUi() {
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