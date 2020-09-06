package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.AppDatabase
import com.tarlad.client.AppSession
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.ChatListDao
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.dao.UserDao
import com.tarlad.client.enums.Events
import com.tarlad.client.helpers.getTitle
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.ChatLists
import com.tarlad.client.models.dto.LastMessage
import com.tarlad.client.repos.MainRepo
import com.tarlad.client.enums.Chats
import io.reactivex.rxjava3.core.Emitter
import io.reactivex.rxjava3.core.Observable
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter as EmitterIO

class MainRepoImpl(
    private val socket: Socket,
    private val database: AppDatabase,
    private val appSession: AppSession,
    private val chatListDao: ChatListDao,
    private val userDao: UserDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) : MainRepo {

    private var addMessageListener: EmitterIO.Listener? = null
    private var updateMessageListener: EmitterIO.Listener? = null
    private var deleteMessageListener: EmitterIO.Listener? = null
    private var addParticipantsListener: EmitterIO.Listener? = null

    override fun getChats(
        time: Long,
        page: Long
    ): Observable<Pair<Chats, List<LastMessage>>> {
        return Observable.create { emitter ->

            val cache = messageDao.getLastMessagesBeforeTime(time, page)
            val cacheList = ArrayList<LastMessage>()
            for (message in cache) {
                val lastMessage = buildLastMessage(message.chatId, message) ?: continue
                cacheList.add(lastMessage)
            }
            if (cacheList.isNotEmpty())
                emitter.onNext(Pair(Chats.ADD, cacheList))

            fetchLastMessages(cache, time, page, cacheList, emitter)
        }
    }

    private fun fetchLastMessages(
        cache: List<Message>,
        time: Long,
        page: Long,
        cacheList: List<LastMessage>,
        emitter: Emitter<Pair<Chats, List<LastMessage>>>
    ) {
        socket.emit("chats/messages/last", time, page, Ack { array ->
            val chats = Gson().fromJson<List<LastMessage>>(
                array[0].toString(),
                object : TypeToken<List<LastMessage>>() {}.type
            )

            if (chats.isEmpty()) {
                if (cache.isNotEmpty()) {
                    messageDao.deleteAll(cache)
                    emitter.onNext(Pair(Chats.DELETE, cacheList))
                } else {
                    emitter.onNext(Pair(Chats.COMPLETE, listOf()))
                }
                emitter.onComplete()
                return@Ack
            }

            val list = ArrayList<LastMessage>()

            for (chat in chats) {
                val cachedUsers = chatListDao.getUsersByChatId(chat.id)
                saveData(Chat(chat.id, chat.title, chat.userId), chat.message, cachedUsers, chat.users)
                val lastMessage = buildLastMessage(chat.id, chat.message) ?: continue
                list.add(lastMessage)
            }

            emitter.onNext(Pair(Chats.ADD, list))
            emitter.onComplete()
        })
    }

    override fun observeChats(): Observable<Pair<Chats, List<LastMessage>>> {
        return Observable.create { emitter ->

            addMessageListener?.let { socket.off("message", it) }
            addMessageListener = EmitterIO.Listener {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                messageDao.insert(message)
                val chat = chatDao.getChatById(message.chatId)
                if (chat == null) {
                    fetchChat(message.chatId, message, emitter)
                } else {
                    val lastMessage = buildLastMessage(chat.id, message) ?: return@Listener
                    val pair = Pair(Chats.ADD, listOf(lastMessage))
                    emitter.onNext(pair)
                }
            }
            socket.on("message", addMessageListener)

            updateMessageListener?.let { socket.off("message/update", it) }
            updateMessageListener = EmitterIO.Listener {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                messageDao.insert(message)
                val chat = chatDao.getChatById(message.chatId)
                if (chat == null) {
//                    fetchChat(message.chatId, message, emitter)
                } else {
                    val last = messageDao.getLastMessageForChat(message.chatId)
                    if (last != message) return@Listener
                    val lastMessage = buildLastMessage(chat.id, message) ?: return@Listener
                    val pair = Pair(Chats.ADD, listOf(lastMessage))
                    emitter.onNext(pair)
                }
            }
            socket.on("message/update", updateMessageListener)

            deleteMessageListener?.let { socket.off("message/delete", it) }
            deleteMessageListener = EmitterIO.Listener { array ->
                val message = messageDao.getById(array[0].toString().toLong()) ?: return@Listener
                messageDao.delete(message)
                val previousMessage = messageDao.getLastMessageForChat(message.chatId)
                if (previousMessage == null) {
                    fetchLastMessage(message.chatId, message, emitter)
                } else {
                    if (message.time < previousMessage.time) return@Listener
                    val lastMessage = buildLastMessage(message.chatId, previousMessage) ?: return@Listener
                    emitter.onNext(Pair(Chats.ADD, listOf(lastMessage)))
                }
            }
            socket.on("message/delete", deleteMessageListener)

            addParticipantsListener?.let { socket.off("chats/update", it) }
            addParticipantsListener = EmitterIO.Listener { array ->
                val chatId = array[0].toString().toLong()
                val cachedUsers = chatListDao.getUsersByChatId(chatId)
                fetchChat(chatId, cachedUsers, emitter)
            }
            socket.on("chats/update", addParticipantsListener)

        }
    }

    private fun fetchChat(
        chatId: Long,
        message: Message,
        emitter: Emitter<Pair<Chats, List<LastMessage>>>
    ) {
        socket.emit(Events.CHATS, chatId, Ack { array ->
            val chatLists = Gson().fromJson(array[0].toString(), ChatLists::class.java)
            saveData(Chat(chatLists.id, chatLists.title, chatLists.userId), null, null, chatLists.users)
            val lastMessage = buildLastMessage(chatLists.id, message) ?: return@Ack
            emitter.onNext(Pair(Chats.ADD, listOf(lastMessage)))
        })
    }

    private fun fetchChat(
        chatId: Long,
        cachedUsers: List<User>,
        emitter: Emitter<Pair<Chats, List<LastMessage>>>
    ) {
        socket.emit(Events.CHATS, chatId, Ack { array ->
            val chatLists = Gson().fromJson(array[0].toString(), ChatLists::class.java)
            chatDao.insert(Chat(chatLists.id, chatLists.title, chatLists.userId))
            removeCachedUsersInChat(cachedUsers, chatLists.id)
            addUsersToChat(chatLists.id, chatLists.users.map { e -> e.id })
            userDao.insertAll(chatLists.users)
            val lastMessage = messageDao.getLastMessageForChat(chatId)
            if (lastMessage != null) {
                val message = buildLastMessage(lastMessage.chatId, lastMessage) ?: return@Ack
                emitter.onNext(Pair(Chats.ADD, listOf(message)))
            }
            fetchLastMessage(chatLists.id, emitter)
        })
    }

    private fun fetchLastMessage(chatId: Long, emitter: Emitter<Pair<Chats, List<LastMessage>>>) {
        socket.emit("messages/last", chatId, Ack { array ->
            val message = Gson().fromJson(array[0].toString(), Message::class.java)
            messageDao.insert(message)
            val lastMessage = buildLastMessage(message.chatId, message) ?: return@Ack
            emitter.onNext(Pair(Chats.ADD, listOf(lastMessage)))
        })
    }

    private fun fetchLastMessage(
        chatId: Long,
        message: Message,
        emitter: Emitter<Pair<Chats, List<LastMessage>>>
    ) {
        socket.emit("messages/last", chatId, Ack { array ->
            if (array[0] == null) {
                val pair = Pair(Chats.DELETE, listOf(LastMessage(chatId, null, -1, message, listOf())))
                emitter.onNext(pair)
                return@Ack
            }
            val loadedPreviousMessage = Gson().fromJson(array[0].toString(), Message::class.java)
            messageDao.insert(loadedPreviousMessage)
            if (message.time < loadedPreviousMessage.time) return@Ack
            val lastMessage = buildLastMessage(chatId, loadedPreviousMessage) ?: return@Ack
            emitter.onNext(Pair(Chats.ADD, listOf(lastMessage)))
        })
    }

    private fun buildLastMessage(chatId: Long, message: Message): LastMessage? {
        val userId = appSession.userId ?: return null
        val chat = chatDao.getChatById(chatId) ?: return null
        val users = chatListDao.getUsersByChatId(chatId)
        val title = getTitle(chat.title, users, userId)
        return LastMessage(chat.id, title, chat.userId, message, users)
    }

    private fun saveData(chat: Chat?, message: Message?, cachedUsers: List<User>?, users: List<User>?){
        message?.let { messageDao.insert(it) }
        chat?.let {
            chatDao.insert(it)
            cachedUsers?.let { list -> removeCachedUsersInChat(list, chat.id) }
            users?.let { list -> addUsersToChat(chat.id, list.map { e -> e.id }) }
        }
        users?.let { userDao.insertAll(it) }
    }

    private fun removeCachedUsersInChat(users: List<User>, chatId: Long) {
        users.forEach { user ->
            chatListDao.delete(ChatList(chatId, user.id))
        }
    }

    private fun addUsersToChat(chatId: Long, users: List<Long>) {
        chatListDao.insert(chatId, users)
    }

    override fun truncate() {
        database.clearAllTables()
    }
}
