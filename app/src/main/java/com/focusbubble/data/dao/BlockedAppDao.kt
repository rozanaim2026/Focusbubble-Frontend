package com.focusbubble.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.focusbubble.data.entities.BlockedApp
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {

    @Query("SELECT * FROM blocked_apps WHERE userId = :userId")
    fun getAllBlockedApps(userId: Int): Flow<List<BlockedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApp(app: BlockedApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedApps(apps: List<BlockedApp>)

    @Delete
    suspend fun deleteBlockedApp(app: BlockedApp)

    @Query("DELETE FROM blocked_apps WHERE userId = :userId")
    suspend fun clearAllForUser(userId: Int)

    @Transaction
    suspend fun replaceAllForUser(
        userId: Int,
        apps: List<BlockedApp>
    ) {
        clearAllForUser(userId)
        insertBlockedApps(apps)
    }
}