package com.focusbubble.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0016\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000f\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010\u001e\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dJ\u0010\u0010\u001f\u001a\u00020 2\u0006\u0010\u001c\u001a\u00020\u001dH\u0002J\u000e\u0010!\u001a\u00020\"2\u0006\u0010\u001c\u001a\u00020\u001dJ\u0010\u0010#\u001a\u00020 2\u0006\u0010\u001c\u001a\u00020\u001dH\u0002J\u000e\u0010$\u001a\u00020%2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010&\u001a\u00020%2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010\'\u001a\u00020\"2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010(\u001a\u00020%2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010)\u001a\u00020*2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010+\u001a\u00020*2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010,\u001a\u00020*2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010-\u001a\u00020*2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010.\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u0010/\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dJ\u000e\u00100\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dJ3\u00101\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u00102\u001a\u00020\u00042\u0019\b\u0002\u00103\u001a\u0013\u0012\u0004\u0012\u000205\u0012\u0004\u0012\u00020\u001b04\u00a2\u0006\u0002\b6H\u0002J\u0016\u00107\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u00108\u001a\u00020*J\u0016\u00109\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010:\u001a\u00020\"J\u0016\u0010;\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010<\u001a\u00020%J\u0016\u0010=\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010>\u001a\u00020*J\u0016\u0010?\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010@\u001a\u00020%J\u0016\u0010A\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010B\u001a\u00020\"J\u0016\u0010C\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010D\u001a\u00020%R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006E"}, d2 = {"Lcom/focusbubble/service/SessionStateManager;", "", "()V", "ACTION_APP_BACKGROUND", "", "ACTION_APP_FOREGROUND", "ACTION_BLOCK_DISMISSED", "ACTION_LEAVE_TO_HOME", "ACTION_SESSION_FINISHED", "ACTION_SESSION_PAUSED", "ACTION_SESSION_RESUMED", "ACTION_SESSION_STOPPED", "ACTION_UPDATE_TIMER", "CELEBRATION_PREFS_NAME", "EXTRA_REMAINING_TIME", "KEY_APP_FOREGROUND", "KEY_COMPLETED_SECONDS", "KEY_IS_PAUSED", "KEY_PENDING_CELEBRATION", "KEY_REMAINING_TIME", "KEY_SESSION_ACTIVE", "KEY_SESSION_END_TIME", "KEY_SESSION_ID", "KEY_TOTAL_DURATION", "PREFS_NAME", "TAG", "clearPendingCelebration", "", "context", "Landroid/content/Context;", "clearSession", "getCelebrationPrefs", "Landroid/content/SharedPreferences;", "getPendingCelebrationSeconds", "", "getPrefs", "getRemainingTime", "", "getSessionEndTime", "getSessionId", "getTotalDuration", "hasPendingCelebration", "", "isAppForeground", "isPaused", "isSessionActive", "pauseSession", "resetPausedFlag", "resumeSession", "sendAppBroadcast", "action", "extras", "Lkotlin/Function1;", "Landroid/content/Intent;", "Lkotlin/ExtensionFunctionType;", "setAppForeground", "isForeground", "setPendingCelebration", "completedSeconds", "setRemainingTime", "timeMs", "setSessionActive", "active", "setSessionEndTime", "endTimeMillis", "setSessionId", "sessionId", "setTotalDuration", "durationMs", "app_debug"})
public final class SessionStateManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "SessionStateManager";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "focus_session_state";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CELEBRATION_PREFS_NAME = "focus_celebration_state";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_IS_PAUSED = "is_paused";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_REMAINING_TIME = "remaining_time_ms";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_SESSION_ID = "session_id";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_SESSION_ACTIVE = "session_active";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_TOTAL_DURATION = "total_duration_ms";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_SESSION_END_TIME = "session_end_time_ms";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_APP_FOREGROUND = "app_foreground";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_PENDING_CELEBRATION = "pending_celebration";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_COMPLETED_SECONDS = "completed_seconds";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SESSION_PAUSED = "com.focusbubble.SESSION_PAUSED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SESSION_RESUMED = "com.focusbubble.SESSION_RESUMED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SESSION_STOPPED = "com.focusbubble.SESSION_STOPPED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SESSION_FINISHED = "com.focusbubble.SESSION_FINISHED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_UPDATE_TIMER = "com.focusbubble.UPDATE_TIMER";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_APP_FOREGROUND = "com.focusbubble.APP_FOREGROUND";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_APP_BACKGROUND = "com.focusbubble.APP_BACKGROUND";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_BLOCK_DISMISSED = "com.focusbubble.BLOCK_DISMISSED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_REMAINING_TIME = "remaining_time";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_LEAVE_TO_HOME = "com.focusbubble.LEAVE_TO_HOME";
    @org.jetbrains.annotations.NotNull()
    public static final com.focusbubble.service.SessionStateManager INSTANCE = null;
    
    private SessionStateManager() {
        super();
    }
    
    private final android.content.SharedPreferences getPrefs(android.content.Context context) {
        return null;
    }
    
    private final android.content.SharedPreferences getCelebrationPrefs(android.content.Context context) {
        return null;
    }
    
    private final void sendAppBroadcast(android.content.Context context, java.lang.String action, kotlin.jvm.functions.Function1<? super android.content.Intent, kotlin.Unit> extras) {
    }
    
    public final void resetPausedFlag(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final boolean isSessionActive(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setSessionActive(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean active) {
    }
    
    public final boolean isPaused(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void pauseSession(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void resumeSession(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final long getRemainingTime(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0L;
    }
    
    public final void setRemainingTime(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long timeMs) {
    }
    
    public final long getTotalDuration(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0L;
    }
    
    public final void setTotalDuration(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long durationMs) {
    }
    
    public final long getSessionEndTime(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0L;
    }
    
    public final void setSessionEndTime(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long endTimeMillis) {
    }
    
    public final boolean isAppForeground(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setAppForeground(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean isForeground) {
    }
    
    public final void setPendingCelebration(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int completedSeconds) {
    }
    
    public final boolean hasPendingCelebration(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final int getPendingCelebrationSeconds(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    public final void clearPendingCelebration(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final int getSessionId(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    public final void setSessionId(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int sessionId) {
    }
    
    public final void clearSession(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}