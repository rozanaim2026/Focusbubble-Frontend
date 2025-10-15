package com.focusbubble.data.repository

import com.focusbubble.data.model.Schedule
import com.focusbubble.data.model.ScheduleCreate
import com.focusbubble.data.network.RetrofitClient
import retrofit2.Response

class ScheduleRepository {

    private val api = RetrofitClient.api

    suspend fun createSchedule(
        userId: Int,
        label: String,
        durationMinutes: Int,
        apps: List<String>
    ): Response<Schedule> {
        return api.createSchedule(
            userId,
            ScheduleCreate(label, durationMinutes, apps, true)
        )
    }

    suspend fun listSchedules(userId: Int): Response<List<Schedule>> {
        return api.listSchedules(userId)
    }

    suspend fun deleteSchedule(userId: Int, scheduleId: Int): Response<Map<String, Boolean>> {
        return api.deleteSchedule(userId, scheduleId)
    }
}