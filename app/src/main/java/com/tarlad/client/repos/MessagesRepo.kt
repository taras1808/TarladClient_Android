package com.tarlad.client.repos

import com.tarlad.client.helpers.TarladResult
import com.tarlad.client.models.*
import io.reactivex.rxjava3.core.Single

interface MessagesRepo {
    suspend fun sendMessage(messageCreator: MessageCreator): TarladResult<Message>
}