package com.focusbubble.data.network;

import com.focusbubble.data.model.*;
import retrofit2.Response;
import retrofit2.http.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0082\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J4\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u00072\u000e\b\u0001\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0004H\u00a7@\u00a2\u0006\u0002\u0010\nJ(\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u00072\b\b\u0001\u0010\r\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u001e\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u00032\b\b\u0001\u0010\u0012\u001a\u00020\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J4\u0010\u0015\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020\u00180\u00160\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u00072\b\b\u0001\u0010\u0019\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u001aJ\u001e\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ$\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020 0\u0003H\u00a7@\u00a2\u0006\u0002\u0010!J\u001e\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00110\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ\u001e\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00110\u00032\b\b\u0001\u0010$\u001a\u00020%H\u00a7@\u00a2\u0006\u0002\u0010&J$\u0010\'\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020(0\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ$\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\f0\u00040\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ\u001e\u0010*\u001a\b\u0012\u0004\u0012\u00020(0\u00032\b\b\u0001\u0010+\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ\u0014\u0010,\u001a\b\u0012\u0004\u0012\u00020-0\u0003H\u00a7@\u00a2\u0006\u0002\u0010!J\u001e\u0010.\u001a\b\u0012\u0004\u0012\u00020(0\u00032\b\b\u0001\u0010+\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ(\u0010/\u001a\b\u0012\u0004\u0012\u00020(0\u00032\b\b\u0001\u0010\u0006\u001a\u00020\u00072\b\b\u0001\u00100\u001a\u000201H\u00a7@\u00a2\u0006\u0002\u00102J\u001e\u00103\u001a\b\u0012\u0004\u0012\u00020(0\u00032\b\b\u0001\u0010+\u001a\u00020\u0007H\u00a7@\u00a2\u0006\u0002\u0010\u001d\u00a8\u00064"}, d2 = {"Lcom/focusbubble/data/network/FocusBubbleApi;", "", "createBlocks", "Lretrofit2/Response;", "", "Lcom/focusbubble/data/model/BlockedAppResponse;", "userId", "", "blocks", "Lcom/focusbubble/data/model/BlockedAppCreate;", "(ILjava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createSchedule", "Lcom/focusbubble/data/model/Schedule;", "schedule", "Lcom/focusbubble/data/model/ScheduleCreate;", "(ILcom/focusbubble/data/model/ScheduleCreate;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "createUser", "Lcom/focusbubble/data/model/User;", "user", "Lcom/focusbubble/data/model/UserCreate;", "(Lcom/focusbubble/data/model/UserCreate;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteSchedule", "", "", "", "scheduleId", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteUser", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActiveBlocks", "getHealth", "Lcom/focusbubble/data/model/HealthResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUser", "googleSignIn", "token", "Lcom/focusbubble/data/model/TokenIn;", "(Lcom/focusbubble/data/model/TokenIn;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "listActiveSessions", "Lcom/focusbubble/data/model/FocusSession;", "listSchedules", "pauseSession", "sessionId", "refreshBlocks", "Lcom/focusbubble/data/model/RefreshBlocksResponse;", "resumeSession", "startSession", "session", "Lcom/focusbubble/data/model/SessionCreate;", "(ILcom/focusbubble/data/model/SessionCreate;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "stopSession", "app_debug"})
public abstract interface FocusBubbleApi {
    
    @retrofit2.http.GET(value = "health")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getHealth(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.HealthResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/google")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object googleSignIn(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.focusbubble.data.model.TokenIn token, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.User>> $completion);
    
    @retrofit2.http.POST(value = "users")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createUser(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.focusbubble.data.model.UserCreate user, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.User>> $completion);
    
    @retrofit2.http.GET(value = "users/{user_id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUser(@retrofit2.http.Path(value = "user_id")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.User>> $completion);
    
    @retrofit2.http.POST(value = "users/{user_id}/schedules")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createSchedule(@retrofit2.http.Path(value = "user_id")
    int userId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.focusbubble.data.model.ScheduleCreate schedule, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.Schedule>> $completion);
    
    @retrofit2.http.GET(value = "users/{user_id}/schedules")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object listSchedules(@retrofit2.http.Path(value = "user_id")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.focusbubble.data.model.Schedule>>> $completion);
    
    @retrofit2.http.DELETE(value = "users/{user_id}/schedules/{schedule_id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteSchedule(@retrofit2.http.Path(value = "user_id")
    int userId, @retrofit2.http.Path(value = "schedule_id")
    int scheduleId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.Map<java.lang.String, java.lang.Boolean>>> $completion);
    
    @retrofit2.http.POST(value = "users/{user_id}/sessions")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object startSession(@retrofit2.http.Path(value = "user_id")
    int userId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.focusbubble.data.model.SessionCreate session, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.FocusSession>> $completion);
    
    @retrofit2.http.GET(value = "users/{user_id}/sessions/active")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object listActiveSessions(@retrofit2.http.Path(value = "user_id")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.focusbubble.data.model.FocusSession>>> $completion);
    
    @retrofit2.http.POST(value = "sessions/{session_id}/pause")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object pauseSession(@retrofit2.http.Path(value = "session_id")
    int sessionId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.FocusSession>> $completion);
    
    @retrofit2.http.POST(value = "sessions/{session_id}/resume")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object resumeSession(@retrofit2.http.Path(value = "session_id")
    int sessionId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.FocusSession>> $completion);
    
    @retrofit2.http.POST(value = "sessions/{session_id}/stop")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object stopSession(@retrofit2.http.Path(value = "session_id")
    int sessionId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.FocusSession>> $completion);
    
    @retrofit2.http.POST(value = "users/{user_id}/blocks")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createBlocks(@retrofit2.http.Path(value = "user_id")
    int userId, @retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    java.util.List<com.focusbubble.data.model.BlockedAppCreate> blocks, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.focusbubble.data.model.BlockedAppResponse>>> $completion);
    
    @retrofit2.http.GET(value = "users/{user_id}/blocks")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getActiveBlocks(@retrofit2.http.Path(value = "user_id")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.focusbubble.data.model.BlockedAppResponse>>> $completion);
    
    @retrofit2.http.POST(value = "refresh_blocks")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object refreshBlocks(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.RefreshBlocksResponse>> $completion);
    
    @retrofit2.http.DELETE(value = "users/{user_id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteUser(@retrofit2.http.Path(value = "user_id")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
}