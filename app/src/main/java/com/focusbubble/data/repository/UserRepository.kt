package com.focusbubble.data.repository

import com.focusbubble.data.model.User
import com.focusbubble.data.model.UserCreate
import com.focusbubble.data.network.RetrofitClient
import retrofit2.Response

class UserRepository {

    private val api = RetrofitClient.api

    suspend fun createUser(email: String, name: String? = null): Response<User> {
        return api.createUser(UserCreate(email, name))
    }

    suspend fun getUser(userId: Int): Response<User> {
        return api.getUser(userId)
    }
}