package com.tarlad.client.repos.impl

import com.tarlad.client.api.ChatsApi
import com.tarlad.client.api.MessageApi
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.ChatListDao
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.dao.UserDao
import com.tarlad.client.helpers.ioIo
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.LastMessage
import com.tarlad.client.repos.MainRepo
import io.reactivex.rxjava3.core.Observable

class MainRepoImpl(
    private val chatListDao: ChatListDao,
    private val userDao: UserDao,
    private val chatDao: ChatDao,
    private val chatsApi: ChatsApi,
    private val messageDao: MessageDao,
    private val messageApi: MessageApi
) : MainRepo {

    override fun getChats(token: String, userId: Long): Observable<List<LastMessage>> {
        return Observable.create { emitter ->

            messageApi.getLastMessages("Bearer $token")
                .ioIo()
                .subscribe(
                    { messages ->
                        messages.forEach { lastMessage ->
                            val last = messageDao.getLastMessageForChat(lastMessage.id)

                            if (last != lastMessage.message) {
                                messageDao.insert(lastMessage.message)
                                chatDao.insert(Chat(lastMessage.id, lastMessage.title))
                            }
                        }
                    },
                    { err -> err.printStackTrace() }
                )

            messageDao.getLastMessage()
                .subscribe(
                    { list ->
                        val newList = arrayListOf<LastMessage>()
                        list.forEach { message ->

                                val chat = chatDao.getChatById(message.chatId)
                                if (chat == null) {
                                    val newChat = chatsApi.getChat("Bearer $token", message.chatId)
                                        .onErrorComplete()
                                        .blockingGet()

                                    if (newChat != null && newChat.users.map { e -> e.id }.contains(userId)) {

                                        chatDao.insert(Chat(newChat.id, newChat.title))
                                        newChat.users.forEach {
                                            chatListDao.insert(ChatList(newChat.id, it.id))
                                        }
                                        val cache = userDao.getUserFromChat(newChat.id)
                                        userDao.insertAll(newChat.users)

                                        val title: String =
                                            newChat.title ?: chatListDao.getUsersNicknameByChatId(
                                                newChat.id,
                                                userId
                                            )
                                                .reduceRight { s, acc -> "$s, $acc" }
                                        newList.add(LastMessage(newChat.id, title, message))

                                    }
                                } else {

                                    val users = chatListDao.getUsersIdByChatId(chat.id)

                                    if (users.isEmpty()) {

                                        val newChat = chatsApi.getChat("Bearer $token", message.chatId)
                                            .onErrorComplete()
                                            .blockingGet()

                                        if (newChat != null && newChat.users.map { e -> e.id }.contains(userId)) {

                                            chatDao.insert(Chat(newChat.id, newChat.title))
                                            newChat.users.forEach {
                                                chatListDao.insert(ChatList(newChat.id, it.id))
                                            }
                                            val cache = userDao.getUserFromChat(newChat.id)
                                            userDao.insertAll(newChat.users)

                                            val title: String =
                                                newChat.title ?:
                                                    chatListDao.getUsersNicknameByChatId(newChat.id, userId)
                                                        .reduceRight { s, acc -> "$s, $acc" }
                                            newList.add(LastMessage(newChat.id, title, message))
                                        }

                                    } else {

                                        if (users.contains(userId)) {

                                            val title: String =
                                                chat.title ?: chatListDao.getUsersNicknameByChatId(
                                                    chat.id,
                                                    userId
                                                )
                                                    .reduceRight { s, acc -> "$s, $acc" }
                                            newList.add(LastMessage(chat.id, title, message))

                                        }
                                    }
                                }
                        }
                        emitter.onNext(newList)
                    },
                    { err -> err.printStackTrace() }
                )
        }
    }
}