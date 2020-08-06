package com.tarlad.client.api

import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.ChatCreator
import com.tarlad.client.models.dto.ChatLists
import com.tarlad.client.models.db.RefreshToken
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface ChatsApi {

    @POST("/api/chats/create")
    fun createChat(@Header("Authorization") token: String, @Body chatCreator: ChatCreator): Single<ChatLists>

    @GET("/api/chats/")
    fun getChats(@Header("Authorization") token: String): Single<List<ChatLists>>

    @GET("/api/chats/{chatId}/")
    fun getChat(@Header("Authorization") token: String, @Path("chatId") chatId: Long): Single<ChatLists>

    @POST("/api/chats/{chatId}/users")
    fun addParticipants(@Header("Authorization") token: String, @Path("chatId") chatId: Long, @Body chatCreator: ChatCreator): Single<Unit>
}