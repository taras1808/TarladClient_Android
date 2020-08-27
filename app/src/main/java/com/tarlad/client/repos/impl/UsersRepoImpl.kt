package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.api.UsersApi
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.dao.UserDao
import com.tarlad.client.helpers.ioIo
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.LastMessage
import com.tarlad.client.repos.UsersRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.socket.client.Ack
import io.socket.client.Socket

class UsersRepoImpl(
    private val socket: Socket,
    private val userDao: UserDao,
    private val usersApi: UsersApi
) : UsersRepo {

    override fun searchUsers(q: String, userId: Long, page: Int): Observable<List<User>> {
        return Observable.create { emitter ->
            val cache = if (q.isEmpty())
                userDao.getAll(userId, page)
            else
                userDao.getByNickname(q, userId, page)

            if (cache.isNotEmpty())
                emitter.onNext(cache)

            if (q.isNotEmpty())
                socket.emit("users/search", q, page, Ack {
                    val users = Gson().fromJson<List<User>>(
                        it[0].toString(),
                        object : TypeToken<List<User>>() {}.type
                    )

                    userDao.deleteAll(cache)
                    userDao.insertAll(users)
                    if (users.isEmpty() && cache.isEmpty()){
                        emitter.onComplete()
                        return@Ack
                    }
                    emitter.onNext(users)
                })
        }
    }

    override fun searchUsersForChat(
        q: String,
        chatId: Long,
        userId: Long,
        page: Int
    ): Observable<List<User>> {
        return Observable.create { emitter ->
            val cache = if (q.isEmpty())
                userDao.getAllForChat(chatId, page).filter { e -> e.id != userId }
            else
                userDao.getByNicknameForChat(q, chatId, page).filter { e -> e.id != userId }

            if (cache.isNotEmpty())
                emitter.onNext(cache)

            if (q.isNotEmpty())
                socket.emit("chats/users/search", chatId, q, page, Ack {
                    val users = Gson().fromJson<List<User>>(
                        it[0].toString(),
                        object : TypeToken<List<User>>() {}.type
                    )
                    userDao.deleteAll(cache)
                    userDao.insertAll(users)
                    if (users.isEmpty() && cache.isEmpty()) {
                        emitter.onComplete()
                        return@Ack
                    }
                    emitter.onNext(users)
                })
        }
    }

    override fun getUsersFromChat(chatId: Long): Observable<List<User>> {
        return Observable.create { emitter ->
            userDao.getDistinctUserFromChat(chatId)
                .subscribe({ emitter.onNext(it) }, {})
        }
    }

    override fun loadProfile(id: Long): Observable<User> {
        return Observable.create { emitter ->

            val cache = userDao.getById(id)

            if (cache != null)
                emitter.onNext(cache)

            socket.emit("users", id, Ack {
                val user = Gson().fromJson(it[0].toString(), User::class.java)
                userDao.insert(user)
                emitter.onNext(user)
                emitter.onComplete()
            })
        }
    }
}
