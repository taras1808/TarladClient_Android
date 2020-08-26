package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.api.ChatsApi
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.ChatListDao
import com.tarlad.client.helpers.ioIo
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.ChatCreator
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.dto.ChatLists
import com.tarlad.client.models.dto.LastMessage
import com.tarlad.client.repos.ChatsRepo
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Single
import io.socket.client.Ack
import io.socket.client.Socket
import retrofit2.http.Header
import java.lang.Exception

class ChatsRepoImpl(
    private val socket: Socket,
    private val chatsApi: ChatsApi,
    private val chatDao: ChatDao,
    private val chatListDao: ChatListDao
) : ChatsRepo {

    override fun createChat(userId: Long, chatCreator: ChatCreator): Single<Chat> {
        return Single.create { emitter ->
            socket.emit("chats/add", chatCreator.data, Ack {
                val chatLists = Gson().fromJson(it[0].toString(), ChatLists::class.java)

                chatDao.insert(Chat(chatLists.id, chatLists.title))
                chatCreator.data.toMutableList().apply { add(userId) }.forEach {
                    chatListDao.insert(ChatList(chatLists.id, it))
                }
                val title = chatLists.title ?: chatLists.users
                            .filter { e -> e.id != userId }
                            .map { e -> e.nickname }
                            .reduceRight { s, acc -> "$s, $acc" }

                emitter.onSuccess(Chat(chatLists.id, title))
            })
        }
    }

    override fun addParticipants(
        chatId: Long,
        chatCreator: ChatCreator
    ): Single<Unit> {
        return Single.create {
            socket.emit("chats/users/add", chatId, chatCreator.data, Ack {
                chatCreator.data.forEach {
                    chatListDao.insert(ChatList(chatId, it))
                }
            })
        }
    }
}
