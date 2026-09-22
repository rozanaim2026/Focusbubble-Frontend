package com.focusbubble.ui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import com.focusbubble.data.entities.BlockedApp;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u001e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u0012\u0010\u0000\u001a\u0004\u0018\u00010\u00012\b\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u001a\u0014\u0010\u0004\u001a\u0004\u0018\u00010\u0005*\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b\u00a8\u0006\t"}, d2 = {"drawableToImageBitmap", "Landroidx/compose/ui/graphics/ImageBitmap;", "drawable", "Landroid/graphics/drawable/Drawable;", "getIconUri", "Landroid/net/Uri;", "Lcom/focusbubble/data/entities/BlockedApp;", "context", "Landroid/content/Context;", "app_debug"})
public final class AppIconUtilsKt {
    
    @org.jetbrains.annotations.Nullable()
    public static final android.net.Uri getIconUri(@org.jetbrains.annotations.NotNull()
    com.focusbubble.data.entities.BlockedApp $this$getIconUri, @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    /**
     * Shared with BlockAppsSheet's icon rendering — used anywhere an app icon needs
     * to go from a PackageManager Drawable to something Compose can display.
     */
    @org.jetbrains.annotations.Nullable()
    public static final androidx.compose.ui.graphics.ImageBitmap drawableToImageBitmap(@org.jetbrains.annotations.Nullable()
    android.graphics.drawable.Drawable drawable) {
        return null;
    }
}