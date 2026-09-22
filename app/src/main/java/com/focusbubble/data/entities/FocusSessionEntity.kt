
package com.focusbubble.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "focus_sessions",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val backendSessionId: Int? = null,
    val plannedDurationMinutes: Int,
    val completedSeconds: Int,
    val status: String,
    val startTime: Long,
    val endTime: Long
)

enum class SessionStatus {
    COMPLETED,
    STOPPED
}