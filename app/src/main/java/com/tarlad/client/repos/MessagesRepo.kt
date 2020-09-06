package com.tarlad.client.repos

import com.tarlad.client.models.db.Message
import com.tarlad.client.enums.Messages
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface MessagesRepo {

    var time: Long

    fun sendMessage(message: Message): Observable<Pair<Messages, List<Message>>>

    fun deleteMessage(message: Message): Single<Pair<Messages, List<Message>>>

    fun editMessage(mes: Message, message: Message): Single<Pair<Messages, List<Message>>>

    fun getMessagesForChatBeforeTime(
        chatId: Long,
    ): Observable<Pair<Messages, List<Message>>>

    fun observeMessages(chatId: Long, userId: Long): Observable<Pair<Messages, List<Message>>>
}
