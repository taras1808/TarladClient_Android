package com.tarlad.client.api

import com.tarlad.client.models.db.User
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface UsersApi {

    @GET("api/search")
    fun searchUsers(
        @Query("q") q: String,
        @Query("userId") userId: Long,
        @Query("page") page: Int = 0
    ): Single<List<User>>

    @GET("api/search/chats/{chatId}/users")
    fun searchUsersForChat(
        @Header("Authorization") token: String,
        @Path("chatId") userId: Long,
        @Query("q") q: String,
        @Query("page") page: Int = 0
    ): Single<List<User>>

}
