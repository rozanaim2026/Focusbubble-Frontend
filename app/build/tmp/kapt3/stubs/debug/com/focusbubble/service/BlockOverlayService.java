package com.focusbubble.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import androidx.compose.ui.platform.ComposeView;
import com.focusbubble.ui.overlay.BlockOverlayViewFactory;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0007\u0018\u0000 &2\u00020\u0001:\u0001&B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0011\u001a\u00020\u0004H\u0002J\b\u0010\u0012\u001a\u00020\u0013H\u0002J\b\u0010\u0014\u001a\u00020\u0013H\u0002J\u0010\u0010\u0015\u001a\u00020\u00132\u0006\u0010\u0016\u001a\u00020\u0017H\u0002J\b\u0010\u0018\u001a\u00020\u0013H\u0002J\u0006\u0010\u0019\u001a\u00020\u001aJ\u0014\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0016J\b\u0010\u001d\u001a\u00020\u0013H\u0016J\b\u0010\u001e\u001a\u00020\u0013H\u0016J\"\u0010\u001f\u001a\u00020 2\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\u0006\u0010!\u001a\u00020 2\u0006\u0010\"\u001a\u00020 H\u0016J\b\u0010#\u001a\u00020\u0013H\u0002J\u0010\u0010$\u001a\u00020\u00132\u0006\u0010\u0011\u001a\u00020\u0004H\u0002J\b\u0010%\u001a\u00020\u0013H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/focusbubble/service/BlockOverlayService;", "Landroid/app/Service;", "()V", "currentBlockedPackage", "", "handler", "Landroid/os/Handler;", "overlayView", "Landroidx/compose/ui/platform/ComposeView;", "remainingTimeMillis", "", "timer", "Landroid/os/CountDownTimer;", "totalDurationMillis", "windowManager", "Landroid/view/WindowManager;", "getAppName", "packageName", "goToHomeUsingActivityIntent", "", "handleEmergencyUse", "handleShowOverlay", "intent", "Landroid/content/Intent;", "hideOverlay", "isOverlayCurrentlyShown", "", "onBind", "Landroid/os/IBinder;", "onCreate", "onDestroy", "onStartCommand", "", "flags", "startId", "removeOverlay", "showOverlay", "startTimerUpdates", "Companion", "app_debug"})
public final class BlockOverlayService extends android.app.Service {
    @org.jetbrains.annotations.Nullable()
    private android.view.WindowManager windowManager;
    @org.jetbrains.annotations.Nullable()
    private androidx.compose.ui.platform.ComposeView overlayView;
    @org.jetbrains.annotations.Nullable()
    private android.os.CountDownTimer timer;
    private long remainingTimeMillis = 0L;
    private long totalDurationMillis = 0L;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler handler = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String currentBlockedPackage;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SHOW_OVERLAY = "com.focusbubble.action.SHOW_OVERLAY";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_HIDE_OVERLAY = "com.focusbubble.action.HIDE_OVERLAY";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_EMERGENCY_USE = "com.focusbubble.action.EMERGENCY_USE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_PACKAGE_NAME = "com.focusbubble.extra.PACKAGE_NAME";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_REMAINING_TIME = "com.focusbubble.extra.REMAINING_TIME";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "BlockOverlayService";
    @org.jetbrains.annotations.Nullable()
    private static com.focusbubble.service.BlockOverlayService instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.focusbubble.service.BlockOverlayService.Companion Companion = null;
    
    public BlockOverlayService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void handleShowOverlay(android.content.Intent intent) {
    }
    
    private final void showOverlay(java.lang.String packageName) {
    }
    
    private final void goToHomeUsingActivityIntent() {
    }
    
    private final void startTimerUpdates() {
    }
    
    private final void hideOverlay() {
    }
    
    private final void handleEmergencyUse() {
    }
    
    private final java.lang.String getAppName(java.lang.String packageName) {
        return null;
    }
    
    public final boolean isOverlayCurrentlyShown() {
        return false;
    }
    
    private final void removeOverlay() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\f\u001a\u00020\rJ\u0006\u0010\u000e\u001a\u00020\rR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/focusbubble/service/BlockOverlayService$Companion;", "", "()V", "ACTION_EMERGENCY_USE", "", "ACTION_HIDE_OVERLAY", "ACTION_SHOW_OVERLAY", "EXTRA_PACKAGE_NAME", "EXTRA_REMAINING_TIME", "TAG", "instance", "Lcom/focusbubble/service/BlockOverlayService;", "isOverlayVisible", "", "isRunning", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final boolean isRunning() {
            return false;
        }
        
        public final boolean isOverlayVisible() {
            return false;
        }
    }
}