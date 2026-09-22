package com.focusbubble;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import com.focusbubble.service.SessionStateManager;
import dagger.hilt.android.HiltAndroidApp;

@dagger.hilt.android.HiltAndroidApp()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0005\u001a\u00020\u0006H\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/focusbubble/FocusBubbleApplication;", "Landroid/app/Application;", "()V", "startedActivityCount", "", "onCreate", "", "app_debug"})
public final class FocusBubbleApplication extends android.app.Application {
    private int startedActivityCount = 0;
    
    public FocusBubbleApplication() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
}