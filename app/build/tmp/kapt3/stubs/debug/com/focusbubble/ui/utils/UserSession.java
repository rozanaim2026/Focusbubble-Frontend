package com.focusbubble.ui.utils;

import android.content.Context;
import java.util.UUID;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\f\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\r\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u000bJ\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u00042\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\n\u001a\u00020\u000bJ\u001e\u0010\u0013\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\u00102\u0006\u0010\u0015\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/focusbubble/ui/utils/UserSession;", "", "()V", "KEY_GUEST_EMAIL", "", "KEY_USER_EMAIL", "KEY_USER_ID", "PREFS_NAME", "clearGuestEmail", "", "context", "Landroid/content/Context;", "clearUser", "getOrCreateGuestEmail", "getUserEmail", "getUserId", "", "isLoggedIn", "", "saveUser", "userId", "email", "app_debug"})
public final class UserSession {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "user_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_USER_ID = "user_id";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_USER_EMAIL = "user_email";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_GUEST_EMAIL = "guest_email";
    @org.jetbrains.annotations.NotNull()
    public static final com.focusbubble.ui.utils.UserSession INSTANCE = null;
    
    private UserSession() {
        super();
    }
    
    public final void saveUser(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int userId, @org.jetbrains.annotations.NotNull()
    java.lang.String email) {
    }
    
    public final int getUserId(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUserEmail(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final boolean isLoggedIn(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    /**
     * Removes ONLY the "who is currently logged in" markers — deliberately NOT
     * a blanket .clear(), which would also wipe KEY_GUEST_EMAIL below and
     * silently break guest identity continuity. Logout must never delete data;
     * it only ends the current session.
     */
    public final void clearUser(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * A stable, device-local identity for "Skip and Continue" users. Generated
     * ONCE and reused on every subsequent Skip tap, so the backend's
     * get_or_create_user() (keyed by email) returns the SAME user row every
     * time instead of creating a brand-new guest identity on every tap.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getOrCreateGuestEmail(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Only for account deletion — an actual new guest identity should be
     * possible after a guest deletes their account.
     */
    public final void clearGuestEmail(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}