package com.tarlad.client.repos

import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.RefreshToken
import com.tarlad.client.models.dto.MessageCreator
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface MessagesRepo {
    fun sendMessage(messageCreator: MessageCreator)
    fun getMessagesForChatBeforeTime(token: String, chatId: Long, time: Long, page: Long): Observable<List<Message>>
    fun getMessagesForChatAfterTime(token: String, chatId: Long, time: Long, page: Long): Observable<List<Message>>
    fun deleteMessage(token: String, id: Long): Single<Unit>
}