package com.tarlad.client.api

import com.tarlad.client.models.Chat
import com.tarlad.client.models.User
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UsersApi {
    @GET("api/search")
    fun searchUsers(
        @Query("q") q: String,
        @Query("user_id") userId: Long
    ): Single<List<User>>


    //TODO ChatApi
    @POST("/pi/chats/create")
    fun createChat(users: List<User>): Single<Chat>
}