package com.tarlad.client.repos.impl

import com.tarlad.client.api.ChatsApi
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.ChatListDao
import com.tarlad.client.helpers.ioIo
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.ChatCreator
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.dto.ChatLists
import com.tarlad.client.repos.ChatsRepo
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Header
import java.lang.Exception

class ChatsRepoImpl(private val chatsApi: ChatsApi, private val chatDao: ChatDao, private val chatListDao: ChatListDao): ChatsRepo {

    override fun createChat(userId: Long, token: String, chatCreator: ChatCreator): Single<Chat> {
        return Single.create {
            val cache: Chat? = chatDao.getChatForUsers()
                .map { chats ->
                    val users = chatListDao.getUsersIdByChatId(chats.id, userId)
                    if (users.sorted() == chatCreator.data.sorted()) chats
                    else Chat(-1, null)
                }
                .map { chat ->
                    if (chat != Chat(-1, null)) {
                        val title =
                            chat.title ?: chatListDao.getUsersNicknameByChatId(chat.id, userId)
                                .reduceRight { s, acc -> "$s, $acc" }
                        Chat(chat.id, title)
                    } else {
                        chat
                    }
                }
                .firstOrNull { e -> e != Chat(-1, null) }

            chatsApi.createChat("Bearer $token", chatCreator)
                .doOnSuccess { chatLists ->
                    chatDao.insert(Chat(chatLists.id, chatLists.title))
                    chatCreator.data.toMutableList().apply { add(userId) }.forEach {
                        chatListDao.insert(ChatList(chatLists.id, it))
                    }
                }
                .subscribe(
                    { chatLists ->
                        val title = chatLists.title ?: chatLists.users
                            .filter { e -> e.id != userId }
                            .map { e -> e.nickname }
                            .reduceRight { s, acc -> "$s, $acc" }
                        it.onSuccess(Chat(chatLists.id, title))
                    },
                    { err ->
                        if (cache != null) it.onSuccess(cache)
                        else it.onError(err)
                    }
                )
        }
    }

    override fun addParticipants(token: String, chatId: Long,  chatCreator: ChatCreator): Single<Unit> {
        return chatsApi.addParticipants("Bearer $token", chatId, chatCreator)
            .doOnSuccess {
                chatCreator.data.forEach {
                    chatListDao.insert(ChatList(chatId, it))
                }
            }
    }
}