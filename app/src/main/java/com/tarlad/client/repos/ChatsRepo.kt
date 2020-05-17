package com.tarlad.client.repos

import com.tarlad.client.helpers.TarladResult
import com.tarlad.client.models.Chat
import com.tarlad.client.models.ChatCreator
import com.tarlad.client.models.User
import io.reactivex.rxjava3.core.Single

interface ChatsRepo {
    suspend fun createChat(chatCreator: ChatCreator): TarladResult<Chat>
}