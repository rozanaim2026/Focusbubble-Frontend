package com.focusbubble.data.repository;

import com.focusbubble.data.dao.BlockedAppDao;
import com.focusbubble.data.entities.BlockedApp;
import com.focusbubble.data.model.BlockedAppCreate;
import com.focusbubble.data.model.BlockedAppResponse;
import com.focusbubble.data.model.RefreshBlocksResponse;
import com.focusbubble.data.network.RetrofitClient;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.Dispatchers;
import retrofit2.Response;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\u000e0\r2\u0006\u0010\u000f\u001a\u00020\u0010J\u0016\u0010\u0011\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0012J0\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u000e0\u00142\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\"\u0010\u001a\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u000e0\u00142\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0012J\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u0014H\u0086@\u00a2\u0006\u0002\u0010\u001dJ\u000e\u0010\u001e\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u001dJ$\u0010\u001f\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010 \u001a\b\u0012\u0004\u0012\u00020\n0\u000eH\u0086@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010!\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0012R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/focusbubble/data/repository/BlockedAppsRepository;", "", "dao", "Lcom/focusbubble/data/dao/BlockedAppDao;", "(Lcom/focusbubble/data/dao/BlockedAppDao;)V", "api", "Lcom/focusbubble/data/network/FocusBubbleApi;", "addBlockedApp", "", "app", "Lcom/focusbubble/data/entities/BlockedApp;", "(Lcom/focusbubble/data/entities/BlockedApp;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "blockedAppsForUser", "Lkotlinx/coroutines/flow/Flow;", "", "userId", "", "clearAllForUser", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createBlocksOnBackend", "Lretrofit2/Response;", "Lcom/focusbubble/data/model/BlockedAppResponse;", "blocks", "Lcom/focusbubble/data/model/BlockedAppCreate;", "(ILjava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBlockedApp", "getActiveBlocksFromBackend", "refreshBlocksOnBackend", "Lcom/focusbubble/data/model/RefreshBlocksResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshFromBackend", "replaceAllForUser", "apps", "syncFromBackend", "app_debug"})
public final class BlockedAppsRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.focusbubble.data.dao.BlockedAppDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.focusbubble.data.network.FocusBubbleApi api = null;
    
    @javax.inject.Inject()
    public BlockedAppsRepository(@org.jetbrains.annotations.NotNull()
    com.focusbubble.data.dao.BlockedAppDao dao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.focusbubble.data.entities.BlockedApp>> blockedAppsForUser(int userId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object clearAllForUser(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addBlockedApp(@org.jetbrains.annotations.NotNull()
    com.focusbubble.data.entities.BlockedApp app, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteBlockedApp(@org.jetbrains.annotations.NotNull()
    com.focusbubble.data.entities.BlockedApp app, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Get active blocked apps from backend for a specific user
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getActiveBlocksFromBackend(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.focusbubble.data.model.BlockedAppResponse>>> $completion) {
        return null;
    }
    
    /**
     * Create blocked apps on backend (when starting a session)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createBlocksOnBackend(int userId, @org.jetbrains.annotations.NotNull()
    java.util.List<com.focusbubble.data.model.BlockedAppCreate> blocks, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.focusbubble.data.model.BlockedAppResponse>>> $completion) {
        return null;
    }
    
    /**
     * Manually trigger backend to expire old blocks
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object refreshBlocksOnBackend(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.RefreshBlocksResponse>> $completion) {
        return null;
    }
    
    /**
     * Sync backend blocks to local database
     * Call this after starting a session to get the blocks created by backend
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncFromBackend(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Legacy method - kept for backward compatibility
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object refreshFromBackend(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object replaceAllForUser(int userId, @org.jetbrains.annotations.NotNull()
    java.util.List<com.focusbubble.data.entities.BlockedApp> apps, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}