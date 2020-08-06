package com.tarlad.client.api

import com.tarlad.client.models.dto.LastMessage
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.dto.MessageCreator
import com.tarlad.client.models.db.RefreshToken
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface MessageApi {

    @GET("api/chats/{chatId}/messages")
    fun getMessagesForChatBeforeTime(@Header("Authorization") token: String, @Path("chatId") chatId: Long, @Query("before") time: Long): Single<List<Message>>


    @GET("api/chats/{chatId}/messages")
    fun getMessagesForChatAfterTime(@Header("Authorization") token: String, @Path("chatId") chatId: Long, @Query("after") time: Long): Single<List<Message>>

    @GET("api/chats/messages/last")
    fun getLastMessages(@Header("Authorization") token: String): Single<List<LastMessage>>
}