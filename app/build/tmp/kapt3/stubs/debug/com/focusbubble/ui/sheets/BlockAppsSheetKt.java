package com.focusbubble.ui.sheets;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.compose.foundation.layout.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import com.focusbubble.ui.utils.UserAppInfo;
import com.focusbubble.ui.viewmodel.BlockedAppsViewModel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000 \n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u0014\u0010\u0006\u001a\u0004\u0018\u00010\u00072\b\u0010\b\u001a\u0004\u0018\u00010\tH\u0002\u00a8\u0006\n"}, d2 = {"BlockAppsSheet", "", "viewModel", "Lcom/focusbubble/ui/viewmodel/BlockedAppsViewModel;", "onDismiss", "Lkotlin/Function0;", "drawableToImageBitmap", "Landroidx/compose/ui/graphics/ImageBitmap;", "drawable", "Landroid/graphics/drawable/Drawable;", "app_debug"})
public final class BlockAppsSheetKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void BlockAppsSheet(@org.jetbrains.annotations.NotNull()
    com.focusbubble.ui.viewmodel.BlockedAppsViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss) {
    }
    
    private static final androidx.compose.ui.graphics.ImageBitmap drawableToImageBitmap(android.graphics.drawable.Drawable drawable) {
        return null;
    }
}