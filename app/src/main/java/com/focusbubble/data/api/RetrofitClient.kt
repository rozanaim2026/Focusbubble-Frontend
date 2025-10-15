package com.focusbubble.data.api

import com.focusbubble.data.model.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body


interface FocusBubbleApi {
    @GET("users/{userId}/blocks/active")
    suspend fun getActiveBlocks(@Path("userId") userId: Int): Response<List<BlockedAppResponse>>

    @POST("auth/google")
    suspend fun googleSignIn(@Body tokenIn: TokenIn): Response<User>

    @POST("users")
    suspend fun createUser(@Body userCreate: UserCreate): Response<User>
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.31.203:8000/api/" // Updated to use your local IP

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: FocusBubbleApi by lazy {
        retrofit.create(FocusBubbleApi::class.java)
    }
}
