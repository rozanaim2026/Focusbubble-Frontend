package com.focusbubble.di

import android.content.Context
import com.focusbubble.data.AppDatabase
import com.focusbubble.data.dao.BlockedAppDao
import com.focusbubble.data.repository.BlockedAppsRepository
import com.focusbubble.data.repository.ScheduleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        // Routed through the shared singleton (AppDatabase.getInstance) instead
        // of building a separate Room.databaseBuilder(...) instance here. This
        // WAS the actual cause of the "IllegalStateException: A migration from
        // 1 to 2 was required but not found" crash on launch — Hilt was
        // providing this app-wide instance to every @Inject'd ViewModel/
        // Repository, but it had no .addMigrations(...) attached, unlike the
        // instance BlockerService used. Two separate Room instances backing the
        // exact same "blocked_apps_db" file, only one of which knew how to
        // upgrade from version 1 to 2 — whichever one opened the database file
        // first "won", and this one crashed immediately when it did.
        //
        // Now there is exactly ONE place a Room instance can ever be created
        // (AppDatabase.getInstance), so this class of bug is no longer possible.
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideBlockedAppDao(database: AppDatabase): BlockedAppDao {
        return database.blockedAppDao()
    }

    @Provides
    @Singleton
    fun provideBlockedAppsRepository(dao: BlockedAppDao): BlockedAppsRepository {
        return BlockedAppsRepository(dao)
    }

    @Provides
    @Singleton
    fun provideScheduleRepository(): ScheduleRepository {
        return ScheduleRepository()
    }
}