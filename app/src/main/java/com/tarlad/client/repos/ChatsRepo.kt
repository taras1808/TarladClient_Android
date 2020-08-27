package com.tarlad.client.repos

import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.ChatCreator
import io.reactivex.rxjava3.core.Single

interface ChatsRepo {
    fun createChat(chatCreator: ChatCreator): Single<Chat>
    fun addParticipants(chatId: Long, chatCreator: ChatCreator): Single<Unit>
}
