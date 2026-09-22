package com.focusbubble.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.focusbubble.data.entities.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun observeSessionsForUser(userId: Int): Flow<List<FocusSessionEntity>>

    @Query("""
        SELECT * FROM focus_sessions
        WHERE userId = :userId AND startTime >= :sinceEpochMillis
        ORDER BY startTime DESC
    """)
    fun observeSessionsSince(userId: Int, sinceEpochMillis: Long): Flow<List<FocusSessionEntity>>

    @Query("""
        SELECT COALESCE(SUM(completedSeconds), 0) FROM focus_sessions
        WHERE userId = :userId AND status = 'COMPLETED' AND startTime >= :sinceEpochMillis
    """)
    suspend fun getCompletedSecondsSince(userId: Int, sinceEpochMillis: Long): Int

    @Query("""
        SELECT COUNT(*) FROM focus_sessions
        WHERE userId = :userId AND status = 'COMPLETED' AND startTime >= :sinceEpochMillis
    """)
    suspend fun getCompletedSessionCountSince(userId: Int, sinceEpochMillis: Long): Int
}
