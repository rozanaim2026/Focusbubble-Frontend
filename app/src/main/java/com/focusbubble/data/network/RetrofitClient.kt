package com.focusbubble.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // CHANGE THIS BASED ON YOUR SETUP:
    // - Android Emulator: "http://10.0.2.2:8000/"
    // - Physical Device: "https://focusbubble-backend.onrender.com/" (Your Mac's IP)
private const val BASE_URL = "https://focusbubble-backend.onrender.com/"

init {
    android.util.Log.d("RetrofitClient", "🌐 BASE_URL = $BASE_URL")
}
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: FocusBubbleApi = retrofit.create(FocusBubbleApi::class.java)
}