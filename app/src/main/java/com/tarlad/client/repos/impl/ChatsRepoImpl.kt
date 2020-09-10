package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.ChatListDao
import com.tarlad.client.dao.UserDao
import com.tarlad.client.enums.Events
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.ChatsRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter as EmitterIO

class ChatsRepoImpl(
    private val socket: Socket,
    private val chatDao: ChatDao,
    private val userDao: UserDao,
    private val chatListDao: ChatListDao
) : ChatsRepo {

    private var addParticipantsListener: EmitterIO.Listener? = null

    override fun createChat(data: List<Long>): Single<Chat> {
        return Single.create { emitter ->
            socket.emit(Events.CHATS_CREATE, data, Ack { array ->
                val chat = Gson().fromJson(array[0].toString(), Chat::class.java)
                chatDao.insert(chat)
                chatListDao.insert(chat.id, data.toMutableList().apply { add(chat.userId) }.toList())
                emitter.onSuccess(chat)
            })
        }
    }

    override fun addParticipants(
        chatId: Long,
        data: List<Long>
    ): Single<Unit> {
        return Single.create { emitter ->
            socket.emit(Events.CHATS_USERS_ADD, chatId, data, Ack { array ->
                if (array.isEmpty()) return@Ack //TODO
                chatListDao.insert(chatId, data)
                emitter.onSuccess(Unit)
            })
        }
    }

    override fun getAdminFromChat(chatId: Long): Single<Long> {
        return Single.create { emitter ->
            val admin = chatDao.getAdmin(chatId)
            emitter.onSuccess(admin)
        }
    }

    override fun removeParticipant(chatId: Long, userId: Long) {
        socket.emit(Events.CHATS_USERS_DELETE, chatId, userId, Ack { array ->
            if (array.isEmpty()) return@Ack //TODO
            chatListDao.delete(ChatList(chatId, userId))
        })
    }

    override fun getTitle(chatId: Long): Single<String> {
        return Single.create { emitter ->
            emitter.onSuccess(chatDao.getTitle(chatId) ?: "")
        }
    }

    override fun saveTitle(chatId: Long, title: String): Single<Unit> {
        return Single.create { emitter ->
            socket.emit(Events.CHATS_TITLE, chatId, title, Ack {
                val chat = Gson().fromJson(it[0].toString(), Chat::class.java)
                chatDao.insert(chat)
                emitter.onSuccess(Unit)
            })
        }
    }

    override fun getChat(id: Long): Observable<Chat> {
        return Observable.create { emitter ->
            val cache = chatDao.getById(id)

            if (cache != null)
                emitter.onNext(cache)

            socket.emit(Events.CHATS, id, Ack { array ->
                if (array.isEmpty()) {
                    emitter.onComplete()
                    return@Ack
                }
                val chat = Gson().fromJson(array[0].toString(), Chat::class.java)
                if (cache != chat) {
                    chatDao.insert(chat)
                    emitter.onNext(chat)
                }
                emitter.onComplete()
            })
        }
    }

    override fun getChatLists(id: Long): Observable<List<User>> {
        return Observable.create { emitter ->
            val cache = chatListDao.getUsersByChatId(id)

            if (cache.isNotEmpty())
                emitter.onNext(cache)

            socket.emit(Events.CHATS_USERS, id, Ack { array ->
                if (array.isEmpty()) {
                    emitter.onComplete()
                    return@Ack
                }
                val users = Gson().fromJson<List<User>>(
                    array[0].toString(),
                    object : TypeToken<List<User>>() {}.type
                ).apply {
                    chatListDao.delete(id, cache)
                    chatListDao.insert(id, this.map { e -> e.id })
                    userDao.insertAll(this)
                }

                if (cache != users) emitter.onNext(users)
                emitter.onComplete()
            })
        }
    }

    override fun observeChats(): Observable<Long> {
        return Observable.create { emitter ->
            addParticipantsListener?.let { socket.off(Events.CHATS_UPDATE, it) }
            addParticipantsListener = EmitterIO.Listener { array ->
                val chatId = array[0].toString().toLong()
                emitter.onNext(chatId)
            }
            socket.on(Events.CHATS_UPDATE, addParticipantsListener)
        }
    }

    override fun observeChat(chatId: Long): Observable<Chat> {
        return Observable.create { emitter ->
            chatDao.observeDistinct(chatId)
                .subscribe({ if (it != null) emitter.onNext(it) }, {})
        }
    }

}
