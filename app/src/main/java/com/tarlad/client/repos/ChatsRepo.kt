package com.tarlad.client.repos

import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.ChatCreator
import io.reactivex.rxjava3.core.Single

interface ChatsRepo {
    fun createChat(userId: Long, token: String, chatCreator: ChatCreator): Single<Chat>
    fun addParticipants(token: String, chatId: Long, chatCreator: ChatCreator): Single<Unit>
}
