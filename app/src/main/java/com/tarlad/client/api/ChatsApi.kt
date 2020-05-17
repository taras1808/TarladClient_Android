package com.tarlad.client.api

import com.tarlad.client.models.Chat
import com.tarlad.client.models.ChatCreator
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatsApi {

    @POST("/api/chats/create")
    fun createChat(@Body chatCreator: ChatCreator): Single<Chat>
}