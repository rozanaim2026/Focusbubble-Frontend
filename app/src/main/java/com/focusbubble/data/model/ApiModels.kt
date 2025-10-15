package com.focusbubble.data.model

import com.google.gson.annotations.SerializedName

// User
data class User(
    val id: Int,
    val email: String,
    val name: String?,
    val picture: String?
)

data class UserCreate(
    val email: String,
    val name: String? = null,
    val picture: String? = null
)

// Schedule
data class Schedule(
    val id: Int,
    val label: String,
    @SerializedName("duration_minutes")
    val durationMinutes: Int,
    val apps: List<String>,
    @SerializedName("is_active")
    val isActive: Boolean,
    @SerializedName("created_at")
    val createdAt: String
)

data class ScheduleCreate(
    val label: String = "Focus",
    @SerializedName("duration_minutes")
    val durationMinutes: Int = 25,
    val apps: List<String> = emptyList(),
    @SerializedName("is_active")
    val isActive: Boolean = false
)

// Session
data class FocusSession(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("schedule_id")
    val scheduleId: Int?,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    val paused: Boolean,
    @SerializedName("remaining_seconds")
    val remainingSeconds: Int?,
    val status: String
)

data class SessionCreate(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("schedule_id")
    val scheduleId: Int? = null,
    @SerializedName("duration_minutes")
    val durationMinutes: Int
)

// Blocked App
data class BlockedAppResponse(
    val id: Int,
    @SerializedName("package_name")
    val packageName: String,
    @SerializedName("app_name")
    val appName: String?,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    @SerializedName("is_active")
    val isActive: Boolean
)

data class BlockedAppCreate(
    @SerializedName("package_name")
    val packageName: String,
    @SerializedName("app_name")
    val appName: String? = null,
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("end_time")
    val endTime: String? = null
)

// Google Auth
data class TokenIn(
    @SerializedName("id_token")
    val idToken: String
)

// Responses
data class HealthResponse(
    val ok: Boolean,
    val time: String
)

data class RefreshBlocksResponse(
    val expired: Int
)

data class BlockedApp(
    val id: Int,
    val packageName: String,
    val appName: String,
    val durationMinutes: Int,
    val is_active: Boolean
)