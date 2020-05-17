package com.tarlad.client.repos.impl

import com.tarlad.client.api.MessageApi
import com.tarlad.client.helpers.TarladResult
import com.tarlad.client.helpers.toCoroutine
import com.tarlad.client.models.Message
import com.tarlad.client.models.MessageCreator
import com.tarlad.client.repos.MessagesRepo

class MessagesRepoImpl(private val messageApi: MessageApi): MessagesRepo {

    override suspend fun sendMessage(messageCreator: MessageCreator): TarladResult<Message>
            = messageApi.createMessage(messageCreator).toCoroutine()
}