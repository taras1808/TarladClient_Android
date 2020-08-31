package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.ChatListDao
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.ChatCreator
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.dto.ChatLists
import com.tarlad.client.repos.ChatsRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.socket.client.Ack
import io.socket.client.Socket

class ChatsRepoImpl(
    private val socket: Socket,
    private val chatDao: ChatDao,
    private val chatListDao: ChatListDao
) : ChatsRepo {

    override fun createChat(chatCreator: ChatCreator): Single<Chat> {
        return Single.create { emitter ->
            socket.emit("chats/add", chatCreator.data, Ack {
                val chatLists = Gson().fromJson(it[0].toString(), ChatLists::class.java)
                chatDao.insert(Chat(chatLists.id, chatLists.title, chatLists.userId))
                chatCreator.data.toMutableList().forEach {
                    chatListDao.insert(ChatList(chatLists.id, it))
                }
                val title = chatLists.title ?: if (chatLists.users.isEmpty()) "" else chatLists.users
                    .map { e -> e.nickname }
                    .reduceRight { s, acc -> "$s, $acc" }

                emitter.onSuccess(Chat(chatLists.id, title, chatLists.userId))
            })
        }
    }

    override fun addParticipants(
        chatId: Long,
        chatCreator: ChatCreator
    ): Single<Unit> {
        return Single.create { emitter ->
            socket.emit("chats/users/add", chatId, chatCreator.data, Ack {
                chatCreator.data.forEach {
                    chatListDao.insert(ChatList(chatId, it))
                }
                emitter.onSuccess(Unit)
            })
        }
    }

    override fun getAdminFromChat(chatId: Long): Observable<Long> {
        return Observable.create { emitter ->
            val admin = chatDao.getAdmin(chatId) ?: return@create
            emitter.onNext(admin)
        }
    }

    override fun removeParticipant(chatId: Long, userId: Long) {
        socket.emit("chats/users/delete", chatId, userId, Ack {
            chatListDao.delete(ChatList(chatId, userId))
        })
    }

}
