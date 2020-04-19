package com.tarlad.client.api

import com.tarlad.client.models.Chat
import com.tarlad.client.models.User
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.POST

interface UsersApi {
    @GET("api/users")
    fun getUsers(): Observable<List<User>>

    @POST("api/createChat")
    fun createChat(users: List<User>): Single<Chat>
}