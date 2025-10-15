package com.focusbubble.data.api;

import com.focusbubble.data.model.*;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Body;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J$\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u00032\b\b\u0001\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001e\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u000f\u001a\u00020\u0010H\u00a7@\u00a2\u0006\u0002\u0010\u0011\u00a8\u0006\u0012"}, d2 = {"Lcom/focusbubble/data/api/FocusBubbleApi;", "", "createUser", "Lretrofit2/Response;", "Lcom/focusbubble/data/model/User;", "userCreate", "Lcom/focusbubble/data/model/UserCreate;", "(Lcom/focusbubble/data/model/UserCreate;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getActiveBlocks", "", "Lcom/focusbubble/data/model/BlockedAppResponse;", "userId", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "googleSignIn", "tokenIn", "Lcom/focusbubble/data/model/TokenIn;", "(Lcom/focusbubble/data/model/TokenIn;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public abstract interface FocusBubbleApi {
    
    @retrofit2.http.GET(value = "users/{userId}/blocks/active")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getActiveBlocks(@retrofit2.http.Path(value = "userId")
    int userId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<java.util.List<com.focusbubble.data.model.BlockedAppResponse>>> $completion);
    
    @retrofit2.http.POST(value = "auth/google")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object googleSignIn(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.focusbubble.data.model.TokenIn tokenIn, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.User>> $completion);
    
    @retrofit2.http.POST(value = "users")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createUser(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.focusbubble.data.model.UserCreate userCreate, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.focusbubble.data.model.User>> $completion);
}