package com.focusbubble.data.repository

import com.focusbubble.data.model.FocusSession
import com.focusbubble.data.model.SessionCreate
import com.focusbubble.data.network.RetrofitClient
import retrofit2.Response

class SessionRepository {

    private val api = RetrofitClient.api

    suspend fun startSession(
        userId: Int,
        scheduleId: Int?,
        durationMinutes: Int
    ): Response<FocusSession> {
        return api.startSession(userId, SessionCreate(userId, scheduleId, durationMinutes))
    }

    suspend fun pauseSession(sessionId: Int): Response<FocusSession> {
        return api.pauseSession(sessionId)
    }

    suspend fun resumeSession(sessionId: Int): Response<FocusSession> {
        return api.resumeSession(sessionId)
    }

    suspend fun stopSession(sessionId: Int): Response<FocusSession> {
        return api.stopSession(sessionId)
    }

    suspend fun listActiveSessions(userId: Int): Response<List<FocusSession>> {
        return api.listActiveSessions(userId)
    }
}