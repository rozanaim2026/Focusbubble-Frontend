package com.focusbubble.data.network

import com.focusbubble.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface FocusBubbleApi {

    // Health
    @GET("health")
    suspend fun getHealth(): Response<HealthResponse>

    // Auth
    @POST("auth/google")
    suspend fun googleSignIn(@Body token: TokenIn): Response<User>

    // Users
    @POST("users")
    suspend fun createUser(@Body user: UserCreate): Response<User>

    @GET("users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: Int): Response<User>

    // Schedules
    @POST("users/{user_id}/schedules")
    suspend fun createSchedule(
        @Path("user_id") userId: Int,
        @Body schedule: ScheduleCreate
    ): Response<Schedule>

    @GET("users/{user_id}/schedules")
    suspend fun listSchedules(@Path("user_id") userId: Int): Response<List<Schedule>>

    @DELETE("users/{user_id}/schedules/{schedule_id}")
    suspend fun deleteSchedule(
        @Path("user_id") userId: Int,
        @Path("schedule_id") scheduleId: Int
    ): Response<Map<String, Boolean>>

    // Sessions
    @POST("users/{user_id}/sessions")
    suspend fun startSession(
        @Path("user_id") userId: Int,
        @Body session: SessionCreate
    ): Response<FocusSession>

    @GET("users/{user_id}/sessions/active")
    suspend fun listActiveSessions(@Path("user_id") userId: Int): Response<List<FocusSession>>

    @POST("sessions/{session_id}/pause")
    suspend fun pauseSession(@Path("session_id") sessionId: Int): Response<FocusSession>

    @POST("sessions/{session_id}/resume")
    suspend fun resumeSession(@Path("session_id") sessionId: Int): Response<FocusSession>

    @POST("sessions/{session_id}/stop")
    suspend fun stopSession(@Path("session_id") sessionId: Int): Response<FocusSession>

    // Blocked Apps
    @POST("users/{user_id}/blocks")
    suspend fun createBlocks(
        @Path("user_id") userId: Int,
        @Body blocks: List<BlockedAppCreate>
    ): Response<List<BlockedAppResponse>>

    @GET("users/{user_id}/blocks")
    suspend fun getActiveBlocks(@Path("user_id") userId: Int): Response<List<BlockedAppResponse>>

    @POST("refresh_blocks")
    suspend fun refreshBlocks(): Response<RefreshBlocksResponse>
}