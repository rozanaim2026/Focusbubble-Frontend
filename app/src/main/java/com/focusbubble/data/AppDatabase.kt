package com.focusbubble.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.focusbubble.data.dao.BlockedAppDao
import com.focusbubble.data.dao.FocusSessionDao
import com.focusbubble.data.dao.UserDao
import com.focusbubble.data.entities.BlockedApp
import com.focusbubble.data.entities.FocusSessionEntity
import com.focusbubble.data.entities.UserEntity

@Database(
    entities = [BlockedApp::class, UserEntity::class, FocusSessionEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun userDao(): UserDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `users` (
                        `id` INTEGER NOT NULL,
                        `email` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `pictureUrl` TEXT,
                        `lastLoginAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `focus_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `backendSessionId` INTEGER,
                        `plannedDurationMinutes` INTEGER NOT NULL,
                        `completedSeconds` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER NOT NULL,
                        FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_focus_sessions_userId` ON `focus_sessions` (`userId`)"
                )
            }
        }

        // blocked_apps had NO user column at all before this — every user on
        // the device shared the exact same block list, same root problem as
        // the weekly-timer bug. DEFAULT -1 orphans existing rows rather than
        // guessing an owner or deleting anything.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE blocked_apps ADD COLUMN userId INTEGER NOT NULL DEFAULT -1")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blocked_apps_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .allowMainThreadQueries()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}