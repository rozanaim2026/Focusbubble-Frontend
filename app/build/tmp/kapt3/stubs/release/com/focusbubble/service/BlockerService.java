package com.focusbubble.service;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.focusbubble.ui.BlockOverlayActivity;
import com.focusbubble.ui.utils.UserSession;
import android.app.usage.UsageStatsManager;
import kotlinx.coroutines.*;
import android.os.CountDownTimer;
import com.focusbubble.data.model.BlockedAppResponse;
import com.focusbubble.data.api.RetrofitClient;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0013\u001a\u00020\u0014H\u0002J\b\u0010\u0015\u001a\u00020\u0016H\u0002J\b\u0010\u0017\u001a\u00020\u0016H\u0002J\b\u0010\u0018\u001a\u00020\u0016H\u0002J\b\u0010\u0019\u001a\u00020\fH\u0002J\u0010\u0010\u001a\u001a\u00020\u00162\u0006\u0010\u001b\u001a\u00020\u0007H\u0002J\u0014\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010 \u001a\u00020\u0016H\u0016J\b\u0010!\u001a\u00020\u0016H\u0016J\"\u0010\"\u001a\u00020\f2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001f2\u0006\u0010#\u001a\u00020\f2\u0006\u0010$\u001a\u00020\fH\u0016J\u0010\u0010%\u001a\u00020\u00162\u0006\u0010&\u001a\u00020\u000eH\u0002J\b\u0010\'\u001a\u00020\u0016H\u0002J\b\u0010(\u001a\u00020\u0016H\u0002J\b\u0010)\u001a\u00020\u0016H\u0002R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/focusbubble/service/BlockerService;", "Landroid/app/Service;", "()V", "blockedApps", "", "Lcom/focusbubble/data/model/BlockedAppResponse;", "channelId", "", "currentBlockedApp", "isPaused", "", "notificationId", "", "remainingTime", "", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "timer", "Landroid/os/CountDownTimer;", "buildNotification", "Landroid/app/Notification;", "checkForegroundApp", "", "createNotificationChannel", "fetchBlockedApps", "getCurrentUserId", "launchBlockOverlay", "packageName", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "flags", "startId", "startTimer", "timeMillis", "stopSession", "togglePauseResume", "updateNotification", "app_release"})
public final class BlockerService extends android.app.Service {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.focusbubble.data.model.BlockedAppResponse> blockedApps;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentBlockedApp;
    private boolean isPaused = false;
    private long remainingTime = 1500000L;
    private final int notificationId = 1;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String channelId = "focus_bubble_blocking_service";
    @org.jetbrains.annotations.Nullable()
    private android.os.CountDownTimer timer;
    
    public BlockerService() {
        super();
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    private final void startTimer(long timeMillis) {
    }
    
    private final void togglePauseResume() {
    }
    
    private final void stopSession() {
    }
    
    private final android.app.Notification buildNotification() {
        return null;
    }
    
    private final void updateNotification() {
    }
    
    private final void createNotificationChannel() {
    }
    
    private final void fetchBlockedApps() {
    }
    
    private final int getCurrentUserId() {
        return 0;
    }
    
    private final void checkForegroundApp() {
    }
    
    private final void launchBlockOverlay(java.lang.String packageName) {
    }
}