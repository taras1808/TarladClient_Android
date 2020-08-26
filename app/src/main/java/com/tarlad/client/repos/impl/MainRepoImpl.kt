package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.ChatListDao
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.dao.UserDao
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.dto.ChatLists
import com.tarlad.client.models.dto.LastMessage
import com.tarlad.client.repos.MainRepo
import com.tarlad.client.ui.views.chat.Messages
import com.tarlad.client.ui.views.main.Chats
import io.reactivex.rxjava3.core.Observable
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter

class MainRepoImpl(
    private val socket: Socket,
    private val chatListDao: ChatListDao,
    private val userDao: UserDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) : MainRepo {

    override fun getChats(userId: Long, time: Long, page: Long): Observable<Pair<Chats, List<LastMessage>>> {
        return Observable.create { emitter ->

            val cache = messageDao.getLastMessagesBeforeTime(time, page)
            var list = ArrayList<LastMessage>()

            cache.forEach { message ->
                val chat = chatDao.getChatById(message.chatId)
                val users = chatListDao.getUsersByChatId(message.chatId, userId)
                val title = chat?.title ?:
                    if (users.isNotEmpty())
                        users.map { it.nickname }
                            .reduceRight { s, acc -> "$s, $acc" }
                    else ""

                list.add(LastMessage(message.chatId, title, message, users))
            }

            if (list.isNotEmpty())
                emitter.onNext(Pair(Chats.ADD, list))
            list = ArrayList()

            socket.emit("chats/messages/last", time, page, Ack {
                val chats = Gson().fromJson<List<LastMessage>>(
                    it[0].toString(),
                    object : TypeToken<List<LastMessage>>() {}.type
                )

                if (chats.isEmpty()) {
                    if (cache.isEmpty()) {
                        emitter.onNext(Pair(Chats.COMPLETE, listOf()))
                        emitter.onComplete()
                    } else {
                        messageDao.deleteAll(cache)
                    }
                    return@Ack
                }

                chats.forEach { chat ->
                    messageDao.insert(chat.message)
                    chatDao.insert(Chat(chat.id, chat.title))
                    val users = chatListDao.getUsersByChatId(chat.id)
                    users.forEach { user ->
                        chatListDao.delete(ChatList(chat.id, user.id))
                    }
                    chat.users.forEach {  user ->
                        chatListDao.insert(ChatList(chat.id, user.id))
                    }
                    userDao.insertAll(chat.users)
                    val title = chat.title ?:
                        chat.users.filter { e -> e.id != userId }
                            .map { e -> e.nickname }
                            .reduceRight { s, acc -> "$s, $acc" }

                    list.add(LastMessage(chat.id, title, chat.message, chat.users))
                }

                emitter.onNext(Pair(Chats.ADD, list))
                emitter.onComplete()
            })
        }
    }

    private var messageListener: Emitter.Listener? = null
    private var delListener: Emitter.Listener? = null

    override fun observeChats(userId: Long): Observable<Pair<Chats, List<LastMessage>>> {
        return Observable.create { emitter ->

            messageListener?.let { socket.off("message", it) }
            messageListener = Emitter.Listener {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                messageDao.insert(message)
                val chat = chatDao.getChatById(message.chatId)
                if (chat == null) {
                    socket.emit("chats", message.chatId, Ack { array ->
                        val chatLists = Gson().fromJson(array[0].toString(), ChatLists::class.java)
                        chatDao.insert(Chat(chatLists.id, chatLists.title))
                        chatLists.users.forEach { user ->
                            chatListDao.insert(ChatList(chatLists.id, user.id))
                        }
                        userDao.insertAll(chatLists.users)
                        val title = chatLists.title ?:
                        chatLists.users
                            .filter { e -> e.id != userId }
                            .map { e -> e.nickname }
                            .reduceRight { s, acc -> "$s, $acc" }
                        emitter.onNext(Pair(Chats.ADD, listOf(LastMessage(chatLists.id, title, message, chatLists.users))))
                    })
                } else {
                    val users = chatListDao.getUsersByChatId(message.chatId, userId)
                    val title = chat.title ?:
                    users.map { e -> e.nickname }
                        .reduceRight { s, acc -> "$s, $acc" }
                    emitter.onNext(Pair(Chats.ADD,listOf(LastMessage(chat.id, title, message, users))))
                }
            }
            socket.on("message", messageListener)

            delListener?.let { socket.off("del", it) }
            delListener = Emitter.Listener {
                messageDao.getById(it[0].toString().toLong())?.let { message ->
                    messageDao.delete(message)

                    val previousMessage = messageDao.getLastMessageForChat(message.chatId)

                    if (previousMessage == null) {
                        socket.emit("chats/last", message.chatId, Ack { array ->
                            val loadedPreviousMessage = Gson().fromJson(array[0].toString(), Message::class.java)

                            if (loadedPreviousMessage.id == 0L) {
                                emitter.onNext(Pair(Chats.DELETE,listOf(LastMessage(message.chatId, null, message, listOf()))))
                                return@Ack
                            }

                            if (message.time < loadedPreviousMessage.time) return@Ack

                            val chat = chatDao.getChatById(message.chatId)!!
                            val users = chatListDao.getUsersByChatId(message.chatId, userId)
                            val title = chat.title ?: users.map { e -> e.nickname }
                                .reduceRight { s, acc -> "$s, $acc" }

                            emitter.onNext(Pair(Chats.ADD,listOf(LastMessage(chat.id, title, loadedPreviousMessage!!, users))))

                        })
                    } else {

                        if (message.time < previousMessage.time) return@let

                        val chat = chatDao.getChatById(message.chatId)!!
                        val users = chatListDao.getUsersByChatId(message.chatId, userId)
                        val title = chat.title ?: users.map { e -> e.nickname }
                            .reduceRight { s, acc -> "$s, $acc" }

                        emitter.onNext(Pair(Chats.ADD,listOf(LastMessage(chat.id, title, previousMessage, users))))
                    }
                }
            }
            socket.on("del", delListener)
        }
    }

    override fun truncate() {
        messageDao.truncate()
        chatDao.truncate()
    }
}
