package com.tarlad.client.repos

import com.tarlad.client.helpers.TarladResult
import com.tarlad.client.models.*
import io.reactivex.rxjava3.core.Single

interface MessagesRepo {
    fun sendMessage(messageCreator: MessageCreator): Single<Message>
    fun getMessagesForChat(chatId: Long): Single<List<Message>>
}