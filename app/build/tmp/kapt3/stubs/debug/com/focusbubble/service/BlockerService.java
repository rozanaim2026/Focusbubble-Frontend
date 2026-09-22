package com.focusbubble.service;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.*;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.focusbubble.MainActivity;
import com.focusbubble.ui.utils.UserSession;
import com.focusbubble.ui.utils.PermissionHelper;
import android.app.usage.UsageStatsManager;
import kotlinx.coroutines.*;
import android.os.CountDownTimer;
import com.focusbubble.data.entities.BlockedApp;
import com.focusbubble.data.entities.FocusSessionEntity;
import com.focusbubble.data.entities.SessionStatus;
import com.focusbubble.data.repository.BlockedAppsRepository;
import com.focusbubble.data.AppDatabase;
import com.focusbubble.data.dao.BlockedAppDao;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u008e\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\"\u001a\u00020#H\u0002J\b\u0010$\u001a\u00020%H\u0002J\b\u0010&\u001a\u00020#H\u0002J\b\u0010\'\u001a\u00020#H\u0002J\u0012\u0010(\u001a\u0004\u0018\u00010\r2\u0006\u0010)\u001a\u00020*H\u0002J\u0012\u0010+\u001a\u0004\u0018\u00010\r2\u0006\u0010)\u001a\u00020*H\u0002J\b\u0010,\u001a\u00020#H\u0002J\u0010\u0010-\u001a\u00020#2\u0006\u0010.\u001a\u00020\rH\u0002J\b\u0010/\u001a\u00020#H\u0002J\u0014\u00100\u001a\u0004\u0018\u0001012\b\u00102\u001a\u0004\u0018\u000103H\u0016J\b\u00104\u001a\u00020#H\u0016J\b\u00105\u001a\u00020#H\u0016J\"\u00106\u001a\u00020\u00142\b\u00102\u001a\u0004\u0018\u0001032\u0006\u00107\u001a\u00020\u00142\u0006\u00108\u001a\u00020\u0014H\u0016J\b\u00109\u001a\u00020#H\u0002J\b\u0010:\u001a\u00020#H\u0002J\b\u0010;\u001a\u00020#H\u0002J\b\u0010<\u001a\u00020#H\u0002J\b\u0010=\u001a\u00020#H\u0002J\b\u0010>\u001a\u00020#H\u0002J\b\u0010?\u001a\u00020#H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082D\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u001cX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001d\u001a\u00020\u001eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0012\u0010\u001f\u001a\u00060 j\u0002`!X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006@"}, d2 = {"Lcom/focusbubble/service/BlockerService;", "Landroid/app/Service;", "()V", "audioFocusChangeListener", "Landroid/media/AudioManager$OnAudioFocusChangeListener;", "audioFocusRequest", "Landroid/media/AudioFocusRequest;", "audioManager", "Landroid/media/AudioManager;", "blockedApps", "", "Lcom/focusbubble/data/entities/BlockedApp;", "channelId", "", "currentBlockedApp", "db", "Lcom/focusbubble/data/AppDatabase;", "isPaused", "", "notificationId", "", "pauseResumeReceiver", "Landroid/content/BroadcastReceiver;", "remainingTime", "", "repository", "Lcom/focusbubble/data/repository/BlockedAppsRepository;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "tickHandler", "Landroid/os/Handler;", "tickRunnable", "Ljava/lang/Runnable;", "Lkotlinx/coroutines/Runnable;", "abandonAudioFocusBlock", "", "buildNotification", "Landroid/app/Notification;", "checkForegroundApp", "createNotificationChannel", "getForegroundPackageFromEvents", "usageStatsManager", "Landroid/app/usage/UsageStatsManager;", "getForegroundPackageFromUsageStats", "handleSessionFinished", "launchBlockOverlay", "packageName", "loadBlockedApps", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "flags", "startId", "requestAudioFocusBlock", "startTicking", "stopSession", "stopTicking", "tick", "togglePauseResume", "updateNotification", "app_debug"})
public final class BlockerService extends android.app.Service {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private com.focusbubble.data.repository.BlockedAppsRepository repository;
    private com.focusbubble.data.AppDatabase db;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.focusbubble.data.entities.BlockedApp> blockedApps;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentBlockedApp;
    private boolean isPaused = false;
    private long remainingTime = 1500000L;
    private final int notificationId = 1;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String channelId = "focus_bubble_blocking_service";
    private android.media.AudioManager audioManager;
    @org.jetbrains.annotations.Nullable()
    private android.media.AudioFocusRequest audioFocusRequest;
    @org.jetbrains.annotations.NotNull()
    private final android.media.AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = null;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler tickHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.Runnable tickRunnable = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.BroadcastReceiver pauseResumeReceiver = null;
    
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
    
    private final void startTicking() {
    }
    
    private final void stopTicking() {
    }
    
    /**
     * Runs every second for as long as the session is active. Checks the
     * persisted pause flag directly on every single tick — this is what makes
     * pause actually stop the countdown reliably, instead of depending on a
     * broadcast arriving to cancel a separate CountDownTimer.
     */
    private final void tick() {
    }
    
    private final void handleSessionFinished() {
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
    
    private final void loadBlockedApps() {
    }
    
    private final void checkForegroundApp() {
    }
    
    /**
     * Event-based foreground detection — far more reliable than queryUsageStats
     * over a tiny window, which was frequently coming back empty ("No usage stats
     * available") and made leaving a blocked app (e.g. via back button) go
     * undetected, leaving the block overlay stuck on screen.
     */
    private final java.lang.String getForegroundPackageFromEvents(android.app.usage.UsageStatsManager usageStatsManager) {
        return null;
    }
    
    private final java.lang.String getForegroundPackageFromUsageStats(android.app.usage.UsageStatsManager usageStatsManager) {
        return null;
    }
    
    private final void requestAudioFocusBlock() {
    }
    
    private final void abandonAudioFocusBlock() {
    }
    
    private final void launchBlockOverlay(java.lang.String packageName) {
    }
}