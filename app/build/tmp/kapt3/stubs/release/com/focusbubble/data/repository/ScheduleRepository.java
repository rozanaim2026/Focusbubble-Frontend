package com.focusbubble.data.repository;

import com.focusbubble.data.model.Schedule;
import com.focusbubble.data.model.ScheduleCreate;
import com.focusbubble.data.network.RetrofitClient;
import retrofit2.Response;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010$\n\u0002\u0010\u000b\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J:\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\t2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000b0\u000eH\u0086@\u00a2\u0006\u0002\u0010\u000fJ0\u0010\u0010\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00120\u00110\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0013\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0014J\"\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00070\u000e0\u00062\u0006\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\u0016R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/focusbubble/data/repository/ScheduleRepository;", "", "()V", "api", "Lcom/focusbubble/data/network/FocusBubbleApi;", "createSchedule", "Lretrofit2/Response;", "Lcom/focusbubble/data/model/Schedule;", "userId", "", "label", "", "durationMinutes", "apps", "", "(ILjava/lang/String;ILjava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteSchedule", "", "", "scheduleId", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "listSchedules", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public final class ScheduleRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.focusbubble.data.network.FocusBubbleApi api = null;
    
    public ScheduleRepository() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createSchedule(int userId, @org.jetbrains.annotations.NotNull()
    java.lang.String label, int durationMinutes, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> apps, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.Schedule>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object listSchedules(int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.focusbubble.data.model.Schedule>>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteSchedule(int userId, int scheduleId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.Map<java.lang.String, java.lang.Boolean>>> $completion) {
        return null;
    }
}