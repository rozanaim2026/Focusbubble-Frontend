package com.focusbubble.data.api;

import com.focusbubble.data.entities.BlockedApp;
import retrofit2.Response;
import retrofit2.http.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00032\b\b\u0001\u0010\t\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\r0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u000f"}, d2 = {"Lcom/focusbubble/data/api/ApiService;", "", "addBlockedApp", "Lretrofit2/Response;", "Lcom/focusbubble/data/entities/BlockedApp;", "app", "(Lcom/focusbubble/data/entities/BlockedApp;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBlockedApp", "", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBlockedApps", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public abstract interface ApiService {
    
    @retrofit2.http.GET(value = "/blocked_apps")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBlockedApps(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.focusbubble.data.entities.BlockedApp>>> $completion);
    
    @retrofit2.http.POST(value = "/blocked_apps")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object addBlockedApp(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.focusbubble.data.entities.BlockedApp app, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.entities.BlockedApp>> $completion);
    
    @retrofit2.http.DELETE(value = "/blocked_apps/{id}")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteBlockedApp(@retrofit2.http.Path(value = "id")
    int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
}