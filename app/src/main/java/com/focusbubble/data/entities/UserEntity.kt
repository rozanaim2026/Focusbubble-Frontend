package com.focusbubble.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val email: String,
    val name: String,
    val pictureUrl: String? = null,
    val lastLoginAt: Long = System.currentTimeMillis()
)