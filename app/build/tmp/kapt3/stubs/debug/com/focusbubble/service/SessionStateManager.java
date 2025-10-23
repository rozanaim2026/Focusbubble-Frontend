package com.focusbubble.service;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton to manage focus session state across services and UI
 * Handles pause/resume state, remaining time, and session ID
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\f\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u000e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u0019\u001a\u00020\u00162\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u001d\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012J\u000e\u0010\u001e\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012J\u0016\u0010\u001f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010 \u001a\u00020\u0016J\u0016\u0010!\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\"\u001a\u00020\u001bJ\u0016\u0010#\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010$\u001a\u00020\u0018J\u0016\u0010%\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010&\u001a\u00020\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/focusbubble/service/SessionStateManager;", "", "()V", "ACTION_SESSION_PAUSED", "", "ACTION_SESSION_RESUMED", "ACTION_SESSION_STOPPED", "ACTION_UPDATE_TIMER", "EXTRA_REMAINING_TIME", "KEY_IS_PAUSED", "KEY_REMAINING_TIME", "KEY_SESSION_ACTIVE", "KEY_SESSION_ID", "KEY_TOTAL_DURATION", "PREFS_NAME", "clearSession", "", "context", "Landroid/content/Context;", "getPrefs", "Landroid/content/SharedPreferences;", "getRemainingTime", "", "getSessionId", "", "getTotalDuration", "isPaused", "", "isSessionActive", "pauseSession", "resumeSession", "setRemainingTime", "timeMs", "setSessionActive", "active", "setSessionId", "sessionId", "setTotalDuration", "durationMs", "app_debug"})
public final class SessionStateManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "focus_session_state";
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
    public static final java.lang.String ACTION_SESSION_PAUSED = "com.focusbubble.SESSION_PAUSED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SESSION_RESUMED = "com.focusbubble.SESSION_RESUMED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_SESSION_STOPPED = "com.focusbubble.SESSION_STOPPED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_UPDATE_TIMER = "com.focusbubble.UPDATE_TIMER";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_REMAINING_TIME = "remaining_time";
    @org.jetbrains.annotations.NotNull()
    public static final com.focusbubble.service.SessionStateManager INSTANCE = null;
    
    private SessionStateManager() {
        super();
    }
    
    private final android.content.SharedPreferences getPrefs(android.content.Context context) {
        return null;
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