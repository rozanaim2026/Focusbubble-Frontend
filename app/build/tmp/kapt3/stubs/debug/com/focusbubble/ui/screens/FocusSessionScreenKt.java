package com.focusbubble.ui.screens;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.widget.Toast;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.StrokeCap;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.core.content.ContextCompat;
import com.focusbubble.R;
import com.focusbubble.data.repository.SessionRepository;
import com.focusbubble.service.BlockerService;
import com.focusbubble.service.SessionStateManager;
import com.focusbubble.ui.utils.UserSession;
import com.focusbubble.ui.viewmodel.FocusStatsViewModel;
import android.util.Log;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\"\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001aH\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\t2\f\u0010\n\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u00a8\u0006\u000b"}, d2 = {"FocusSessionScreen", "", "durationMinutes", "", "focusStatsViewModel", "Lcom/focusbubble/ui/viewmodel/FocusStatsViewModel;", "onStop", "Lkotlin/Function0;", "onSessionComplete", "Lkotlin/Function1;", "onFocusSessionsClick", "app_debug"})
public final class FocusSessionScreenKt {
    
    @androidx.compose.runtime.Composable()
    public static final void FocusSessionScreen(int durationMinutes, @org.jetbrains.annotations.NotNull()
    com.focusbubble.ui.viewmodel.FocusStatsViewModel focusStatsViewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStop, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onSessionComplete, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onFocusSessionsClick) {
    }
}