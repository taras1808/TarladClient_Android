package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.dto.MessageCreator
import com.tarlad.client.repos.MessagesRepo
import com.tarlad.client.ui.views.chat.Messages
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

    override fun sendMessage(
        messageCreator: MessageCreator,
        userId: Long
    ): Observable<Pair<Messages, List<Message>>> {
        return Observable.create { emitter ->
            val message = JSONObject()
            message.put("chatId", messageCreator.chatId)
            message.put("data", messageCreator.data)
            message.put("type", messageCreator.type)
            message.put("time", messageCreator.time)

            val mes = Message(
                -1,
                messageCreator.chatId,
                userId,
                messageCreator.type,
                messageCreator.data,
                messageCreator.time
            )

            emitter.onNext(Pair(Messages.SEND, listOf(mes)))

            socket.emit("messages", message, Ack {
                val sentMessage: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                messageDao.insert(sentMessage)
                emitter.onNext(Pair(Messages.REPLACE, listOf(mes, sentMessage)))
                emitter.onComplete()
            })
        }
    }

    override fun deleteMessage(id: Long): Single<Pair<Messages, List<Message>>> {
        return Single.create { emitter ->
            emitter.onSuccess(
                Pair(
                    Messages.DELETE,
                    listOf(Message(id, -1, -1, "", "", 0))
                )
            )
            if (id != -1L)
                socket.emit("messages/delete", id)
        }
    }

    override fun getMessagesForChatBeforeTime(
        chatId: Long,
        time: Long,
        page: Long
    ): Observable<Pair<Messages, List<Message>>> {
        return Observable.create { emitter ->
            val cache = messageDao.getMessagesForChatBeforeTime(chatId, time, page)

            if (cache.isNotEmpty())
                emitter.onNext(Pair(Messages.ADD, cache))

            socket.emit("chats/messages/before", chatId, time, page, Ack {
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
                    }
                    return@Ack
                }

                if (cache.size != messages.size || !cache.containsAll(messages)) {
                    messageDao.deleteAll(cache)
                    messageDao.insertAll(messages)
                    emitter.onNext(Pair(Messages.REMOVE, cache.subtract(messages).toList()))
                    emitter.onNext(Pair(Messages.UPDATE, messages.subtract(cache).toList()))
                    emitter.onComplete()
                }
            })
        }
    }

    private var messageListener: Emitter.Listener? = null
    private var delListener: Emitter.Listener? = null

    override fun observeMessages(
        chatId: Long,
        userId: Long
    ): Observable<Pair<Messages, List<Message>>> {
        return Observable.create { emitter ->

            messageListener?.let { socket.off("message", it) }
            messageListener = Emitter.Listener {
                val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
                if (message.userId != userId)
                    emitter.onNext(Pair(Messages.ADD, listOf(message)))
            }
            socket.on("message", messageListener)

            delListener?.let { socket.off("del", it) }
            delListener = Emitter.Listener {
                emitter.onNext(
                    Pair(
                        Messages.DELETE,
                        listOf(Message(it[0].toString().toLong(), -1, -1, "", "", 0))
                    )
                )
            }
            socket.on("del", delListener)

        }
    }
}