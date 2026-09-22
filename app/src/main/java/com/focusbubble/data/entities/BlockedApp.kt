package com.focusbubble.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedApp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // Default -1 for the migration below — any pre-existing local rows become
    // orphaned (invisible to every real user, since no real userId is ever
    // -1) rather than deleted outright or wrongly attributed to whichever
    // user happens to be logged in first after the update.
    val userId: Int = -1,
    val packageName: String,
    val appName: String,
    val durationMinutes: Int,
    val is_active: Boolean = true
)