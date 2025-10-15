package com.focusbubble;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.compose.runtime.Composable;
import androidx.core.content.ContextCompat;
import com.focusbubble.service.BlockerService;
import com.focusbubble.ui.screens.*;
import com.focusbubble.ui.viewmodel.BlockedAppsViewModel;
import com.focusbubble.ui.viewmodel.FocusStatsViewModel;
import com.jakewharton.threetenabp.AndroidThreeTen;
import dagger.hilt.android.AndroidEntryPoint;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u0018\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\u001a$\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u00a8\u0006\u0007"}, d2 = {"AppNavHost", "", "sharedPrefs", "Landroid/content/SharedPreferences;", "onStartFocusClick", "Lkotlin/Function1;", "", "app_debug"})
public final class MainActivityKt {
    
    @androidx.compose.runtime.Composable()
    public static final void AppNavHost(@org.jetbrains.annotations.NotNull()
    android.content.SharedPreferences sharedPrefs, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onStartFocusClick) {
    }
}