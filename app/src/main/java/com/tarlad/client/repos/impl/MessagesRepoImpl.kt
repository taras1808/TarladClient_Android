package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.enums.Events
import com.tarlad.client.models.db.Message
import com.tarlad.client.repos.MessagesRepo
import com.tarlad.client.enums.Messages
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject

class MessagesRepoImpl(
    private val socket: Socket,
    private val messageDao: MessageDao
) : MessagesRepo {

    private var messageListener: Emitter.Listener? = null
    private var updateMessageListener: Emitter.Listener? = null
    private var delListener: Emitter.Listener? = null
    override var time: Long = Long.MAX_VALUE

    override fun sendMessage(message: Message): Observable<Pair<Messages, List<Message>>> {
        return Observable.create { emitter ->
            val data = JSONObject()
            data.put("chatId", message.chatId)
            data.put("data", message.data)
            data.put("type", message.type)
            data.put("time", message.time)

            emitter.onNext(Pair(Messages.SEND, listOf(message)))
            socket.emit(Events.MESSAGES, data, Ack {
                val sentMessage: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                messageDao.insert(sentMessage)
                emitter.onNext(Pair(Messages.REPLACE, listOf(message, sentMessage)))
                emitter.onComplete()
            })

        }
    }

    override fun deleteMessage(message: Message): Single<Pair<Messages, List<Message>>> {
        return Single.create { emitter ->
            emitter.onSuccess(Pair(Messages.DELETE, listOf(message)))
            socket.emit(Events.MESSAGES_DELETE, message.id)
        }
    }

    override fun editMessage(mes: Message, message: Message): Single<Pair<Messages, List<Message>>> {
        return Single.create { emitter ->
            socket.emit("messages/edit", message.id, message.data, Ack {
                val editedMessage: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                messageDao.insert(editedMessage)
                emitter.onSuccess(Pair(Messages.REPLACE, listOf(mes, editedMessage)))
            })
        }
    }

    override fun getMessagesForChatBeforeTime(
        chatId: Long
    ): Observable<Pair<Messages, List<Message>>> {
        return Observable.create { emitter ->

            val cache = messageDao.getMessagesForChatBeforeTime(chatId, time)

            if (cache.isNotEmpty())
                emitter.onNext(Pair(Messages.ADD, cache))

            socket.emit("chats/messages/before", chatId, time, Ack {

                if (it.isEmpty()) {
                    emitter.onComplete()
                    return@Ack
                }

                val messages = Gson().fromJson<List<Message>>(
                    it[0].toString(),
                    object : TypeToken<List<Message>>() {}.type
                )

                if (messages.isEmpty()) {
                    if (cache.isEmpty()) {
                        emitter.onNext(Pair(Messages.COMPLETE, listOf()))
                        emitter.onComplete()
                    } else {
                        messageDao.deleteAll(cache)
                        emitter.onNext(Pair(Messages.DELETE, cache))
                    }
                    return@Ack
                }

                if (cache.size != messages.size || !cache.containsAll(messages)) {

                    time = messages.minOf { e -> e.time }
                    messageDao.deleteAll(cache)
                    messageDao.insertAll(messages)
                    emitter.onNext(Pair(Messages.REMOVE, cache.subtract(messages).toList()))
                    emitter.onNext(Pair(Messages.UPDATE, messages.subtract(cache).toList()))
                    emitter.onComplete()
                }
            })

            time = cache.minOfOrNull { e -> e.time } ?: time
        }
    }

    override fun observeMessages(
        chatId: Long,
        userId: Long
    ): Observable<Pair<Messages, List<Message>>> {
        return Observable.create { emitter ->

            messageListener?.let { socket.off(Events.MESSAGES, it) }
            messageListener = Emitter.Listener {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                if (message.chatId == chatId)
                    emitter.onNext(Pair(Messages.ADD, listOf(message)))
            }
            socket.on(Events.MESSAGES, messageListener)

            updateMessageListener?.let { socket.off(Events.MESSAGES_UPDATE, it) }
            updateMessageListener = Emitter.Listener {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                if (message.chatId == chatId)
                    emitter.onNext(Pair(Messages.UPDATE, listOf(message)))
            }
            socket.on(Events.MESSAGES_UPDATE, updateMessageListener)

            delListener?.let { socket.off(Events.MESSAGES_DELETE, it) }
            delListener = Emitter.Listener {
                emitter.onNext(
                    Pair(
                        Messages.DELETE,
                        listOf(Message(it[0].toString().toLong(), -1, -1, "", "", 0))
                    )
                )
            }
            socket.on(Events.MESSAGES_DELETE, delListener)

        }
    }
}