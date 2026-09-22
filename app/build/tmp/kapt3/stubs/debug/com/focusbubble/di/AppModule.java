package com.focusbubble.di;

import android.content.Context;
import com.focusbubble.data.AppDatabase;
import com.focusbubble.data.dao.BlockedAppDao;
import com.focusbubble.data.repository.BlockedAppsRepository;
import com.focusbubble.data.repository.ScheduleRepository;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Singleton;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\u0007J\u0012\u0010\n\u001a\u00020\u00062\b\b\u0001\u0010\u000b\u001a\u00020\fH\u0007J\b\u0010\r\u001a\u00020\u000eH\u0007\u00a8\u0006\u000f"}, d2 = {"Lcom/focusbubble/di/AppModule;", "", "()V", "provideBlockedAppDao", "Lcom/focusbubble/data/dao/BlockedAppDao;", "database", "Lcom/focusbubble/data/AppDatabase;", "provideBlockedAppsRepository", "Lcom/focusbubble/data/repository/BlockedAppsRepository;", "dao", "provideDatabase", "context", "Landroid/content/Context;", "provideScheduleRepository", "Lcom/focusbubble/data/repository/ScheduleRepository;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.focusbubble.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.focusbubble.data.AppDatabase provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @dagger.Provides()
    @org.jetbrains.annotations.NotNull()
    public final com.focusbubble.data.dao.BlockedAppDao provideBlockedAppDao(@org.jetbrains.annotations.NotNull()
    com.focusbubble.data.AppDatabase database) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.focusbubble.data.repository.BlockedAppsRepository provideBlockedAppsRepository(@org.jetbrains.annotations.NotNull()
    com.focusbubble.data.dao.BlockedAppDao dao) {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.focusbubble.data.repository.ScheduleRepository provideScheduleRepository() {
        return null;
    }
}