package com.tarlad.client.repos.impl

import com.tarlad.client.api.ChatsApi
import com.tarlad.client.helpers.TarladResult
import com.tarlad.client.helpers.toCoroutine
import com.tarlad.client.models.Chat
import com.tarlad.client.models.ChatCreator
import com.tarlad.client.repos.ChatsRepo

class ChatsRepoImpl(private val chatsApi: ChatsApi): ChatsRepo {

    override suspend fun createChat(chatCreator: ChatCreator): TarladResult<Chat>
            = chatsApi.createChat(chatCreator).toCoroutine()
}