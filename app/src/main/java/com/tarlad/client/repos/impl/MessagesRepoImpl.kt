package com.tarlad.client.repos.impl

import com.tarlad.client.AppSession
import com.tarlad.client.api.AuthApi
import com.tarlad.client.api.MessageApi
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.helpers.ioIo
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.dto.MessageCreator
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.repos.MessagesRepo
import com.tarlad.client.states.AppStates
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.socket.client.Socket
import org.json.JSONObject
import org.reactivestreams.Publisher
import retrofit2.HttpException
import java.net.ConnectException
import java.util.*

class MessagesRepoImpl(
    private val socket: Socket,
    private val appSession: AppSession,
    private val messageApi: MessageApi,
    private val messageDao: MessageDao,
    private val authRepo: AuthRepo,
    private val tokenDao: TokenDao
) : MessagesRepo {

    override fun sendMessage(messageCreator: MessageCreator) {
        val message = JSONObject()
        message.put("chatId", messageCreator.chatId)
        message.put("data", messageCreator.data)
        message.put("type", messageCreator.type)
        message.put("time", messageCreator.time)
        socket.emit("messages", message)
    }

    //TODO
    override fun getMessagesForChatAfterTime(token: String, chatId: Long, time: Long, page: Long): Observable<List<Message>> {
        return Observable.create { emitter ->

            messageDao.getDistinctMessagesForChatAfterTimeObservable(chatId, time, page)
                .subscribe({
                    emitter.onNext(it)

                    if (it.isEmpty())
                        messageApi.getMessagesForChatAfterTime("Bearer $token", chatId, time, page)
                            .ioIo()
                            .subscribe(
                                { messages -> messageDao.insertAll(messages) },
                                { err -> err.printStackTrace() }
                            )
                }, {})


        }
    }

    override fun getMessagesForChatBeforeTime(token: String, chatId: Long, time: Long, page: Long): Observable<List<Message>> {
        return Observable.create { emitter ->

            val cache = messageDao.getMessagesForChatBeforeTime(chatId, time, page)

            val toDispose = messageDao.getDistinctMessagesForChatObservable(chatId, time, page)
                .subscribe({ emitter.onNext(it) }, {})

            messageApi.getMessagesForChatBeforeTime("Bearer $token", chatId, time, page)
                .ioIo()
                .subscribe(
                    { messages ->

                        if (messages.isEmpty()) {
                            if (cache.isEmpty()) {
                                emitter.onComplete()
                            } else {
                                messageDao.deleteAll(cache)
                            }
                            toDispose.dispose()
                            return@subscribe
                        }

                        if (cache.size != messages.size || !cache.containsAll(messages)) {
                            messageDao.deleteAll(cache)
                            messageDao.insertAll(messages)
                            messageDao.getDistinctMessagesForChatObservable(chatId, time, page)
                                .takeWhile { it != messages }
                                .subscribe({ messageDao.deleteAll(it) }, {})
                        }
                    }, { err -> err.printStackTrace() }
                )
        }
    }

    override fun deleteMessage(token: String, id: Long): Single<Unit> {
        return messageApi.deleteMessage("Bearer $token", id)
    }
}