package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.AppDatabase
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.models.db.Message
import com.tarlad.client.repos.MainRepo
import com.tarlad.client.enums.Chats
import com.tarlad.client.enums.Events
import io.reactivex.rxjava3.core.Emitter
import io.reactivex.rxjava3.core.Observable
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter as EmitterIO

class MainRepoImpl(
    private val socket: Socket,
    private val database: AppDatabase,
    private val messageDao: MessageDao
) : MainRepo {

    private var addMessageListener: EmitterIO.Listener? = null
    private var updateMessageListener: EmitterIO.Listener? = null
    private var deleteMessageListener: EmitterIO.Listener? = null

    override fun getMessages(
        time: Long,
        page: Long
    ): Observable<Pair<Chats, List<Message>>> {
        return Observable.create { emitter ->
            val cache = messageDao.getLastMessagesBeforeTime(time, page)
            if (cache.isNotEmpty())
                emitter.onNext(Pair(Chats.ADD, cache))
            fetchLastMessages(cache, time, page, emitter)
        }
    }

    private fun fetchLastMessages(
        cache: List<Message>,
        time: Long,
        page: Long,
        emitter: Emitter<Pair<Chats, List<Message>>>
    ) {
        socket.emit(Events.CHATS_MESSAGES_LAST, time, page, Ack { array ->
            val messages = Gson().fromJson<List<Message>>(
                array[0].toString(),
                object : TypeToken<List<Message>>() {}.type
            )

            if (messages.isEmpty()) {
                if (cache.isNotEmpty()) {
                    messageDao.deleteAll(cache)
                    emitter.onNext(Pair(Chats.DELETE, cache))
                } else {
                    emitter.onNext(Pair(Chats.COMPLETE, listOf()))
                }
                emitter.onComplete()
                return@Ack
            }
            if (cache != messages) {
                emitter.onNext(Pair(Chats.ADD, messages))
                messageDao.insertAll(messages)
            }
            emitter.onComplete()
        })
    }

    override fun observeMessages(): Observable<Pair<Chats, List<Message>>> {
        return Observable.create { emitter ->

            addMessageListener?.let { socket.off(Events.MESSAGES, it) }
            addMessageListener = EmitterIO.Listener {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                messageDao.insert(message)
                if (messageDao.getLastMessageForChat(message.chatId)?.time == message.time)
                    emitter.onNext(Pair(Chats.ADD, listOf(message)))
            }
            socket.on(Events.MESSAGES, addMessageListener)

            updateMessageListener?.let { socket.off(Events.MESSAGES_UPDATE, it) }
            updateMessageListener = EmitterIO.Listener {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                messageDao.insert(message)
                if (messageDao.getLastMessageForChat(message.chatId)?.time == message.time)
                    emitter.onNext(Pair(Chats.ADD, listOf(message)))
            }
            socket.on(Events.MESSAGES_UPDATE, updateMessageListener)

            deleteMessageListener?.let { socket.off(Events.MESSAGES_DELETE, it) }
            deleteMessageListener = EmitterIO.Listener { array ->
                val message = messageDao.getById(array[0].toString().toLong()) ?: return@Listener
                messageDao.delete(message)
                val previousMessage = messageDao.getLastMessageForChat(message.chatId)
                if (previousMessage == null) {
                    fetchLastMessageForChat(message.chatId, message, emitter)
                } else {
                    if (message.time < previousMessage.time) return@Listener
                    emitter.onNext(Pair(Chats.ADD, listOf(previousMessage)))
                }
            }
            socket.on(Events.MESSAGES_DELETE, deleteMessageListener)

        }
    }

    private fun fetchLastMessageForChat(
        chatId: Long,
        message: Message,
        emitter: Emitter<Pair<Chats, List<Message>>>
    ) {
        socket.emit(Events.MESSAGES_LAST, chatId, Ack { array ->
            if (array.isEmpty()) {
                val pair = Pair(Chats.DELETE, listOf(message))
                emitter.onNext(pair)
                return@Ack
            }
            val loadedPreviousMessage = Gson().fromJson(array[0].toString(), Message::class.java)
            messageDao.insert(loadedPreviousMessage)
            if (message.time < loadedPreviousMessage.time) return@Ack
            emitter.onNext(Pair(Chats.ADD, listOf(loadedPreviousMessage)))
        })
    }

    override fun truncate() {
        database.clearAllTables()
    }
}
