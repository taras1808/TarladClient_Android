package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.AppSession
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.ChatListDao
import com.tarlad.client.dao.UserDao
import com.tarlad.client.enums.Chats
import com.tarlad.client.enums.Events
import com.tarlad.client.helpers.getTitle
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.ChatCreator
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.ChatLists
import com.tarlad.client.repos.ChatsRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.socket.client.Ack
import io.socket.client.Socket
import io.socket.emitter.Emitter as EmitterIO

class ChatsRepoImpl(
    private val socket: Socket,
    private val appSession: AppSession,
    private val chatDao: ChatDao,
    private val userDao: UserDao,
    private val chatListDao: ChatListDao
) : ChatsRepo {

    override fun createChat(chatCreator: ChatCreator): Single<Chat> {
        return Single.create { emitter ->
            val userId = appSession.userId ?: return@create
            socket.emit(Events.CHATS_CREATE, chatCreator.data, Ack {
                val chatLists = Gson().fromJson(it[0].toString(), ChatLists::class.java)
                chatDao.insert(Chat(chatLists.id, chatLists.title, chatLists.userId))
//                chatListDao.insert(chatLists.id, chatCreator.data.toMutableList().apply { add(userId) }.toList())
                val title = getTitle(chatLists.title, chatLists.users, userId)
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
                if (it.isEmpty()) return@Ack
//                chatListDao.insert(chatId, chatCreator.data)
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

    override fun getTitle(chatId: Long): Single<String> {
        return Single.create { emitter ->
            emitter.onSuccess(chatDao.getTitle(chatId) ?: "")
        }
    }

    override fun saveTitle(chatId: Long, title: String): Single<Unit> {
        return Single.create { emitter ->
            socket.emit("chats/title", chatId, title, Ack {
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
                val chat = Gson().fromJson(array[0].toString(), Chat::class.java)
                chatDao.insert(chat)
                emitter.onNext(chat)
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
                val users = Gson().fromJson<List<User>>(
                    array[0].toString(),
                    object : TypeToken<List<User>>() {}.type
                ).apply {
                    chatListDao.delete(id, cache)
                    chatListDao.insert(id, this)
                    userDao.insertAll(this)
                }

                if (cache != users) emitter.onNext(users)
                emitter.onComplete()
            })
        }
    }


    private var addParticipantsListener: EmitterIO.Listener? = null


    override fun observeChats(): Observable<Long> {
        return Observable.create { emitter ->
            addParticipantsListener?.let { socket.off("chats/update", it) }
            addParticipantsListener = EmitterIO.Listener { array ->
                val chatId = array[0].toString().toLong()
                emitter.onNext(chatId)
            }
            socket.on("chats/update", addParticipantsListener)
        }
    }

    override fun observeChat(chatId: Long): Observable<Chat> {
        return Observable.create { emitter ->
            chatDao.observeDistinct(chatId)
                .subscribe({ if (it != null) emitter.onNext(it) }, {})
        }
    }

}
