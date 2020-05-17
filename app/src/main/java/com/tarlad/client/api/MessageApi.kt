package com.tarlad.client.api

import com.tarlad.client.models.Message
import com.tarlad.client.models.MessageCreator
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface MessageApi {

    @POST("api/messages/create")
    fun createMessage(@Body messageCreator: MessageCreator): Single<Message>
}