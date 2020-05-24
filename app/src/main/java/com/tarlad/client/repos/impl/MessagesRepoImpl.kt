package com.tarlad.client.repos.impl

import com.tarlad.client.api.MessageApi
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.models.Message
import com.tarlad.client.models.MessageCreator
import com.tarlad.client.repos.MessagesRepo
import io.reactivex.rxjava3.core.Single

class MessagesRepoImpl(private val messageApi: MessageApi, private val messageDao: MessageDao) :
    MessagesRepo {

    override fun sendMessage(messageCreator: MessageCreator): Single<Message> {
        return messageApi.createMessage(messageCreator)
            .doOnSuccess {
                messageDao.insert(it)
            }
    }

    override fun getMessagesForChat(chatId: Long): Single<List<Message>> {
        return Single.create {
            it.onSuccess(messageDao.getMessagesForChat(chatId))
        }
    }
}