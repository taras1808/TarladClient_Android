package com.tarlad.client.api

import com.tarlad.client.models.dto.ChatCreator
import com.tarlad.client.models.dto.ChatLists
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface ChatsApi {

    @POST("/api/chats/create")
    fun createChat(@Header("Authorization") token: String, @Body chatCreator: ChatCreator): Single<ChatLists>

    @POST("/api/chats/{chatId}/users")
    fun addParticipants(@Header("Authorization") token: String, @Path("chatId") chatId: Long, @Body chatCreator: ChatCreator): Single<Unit>
}
