package com.tarlad.client.repos.impl

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tarlad.client.dao.UserDao
import com.tarlad.client.enums.Events
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.UsersRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.socket.client.Ack
import io.socket.client.Socket

class UsersRepoImpl(
    private val socket: Socket,
    private val userDao: UserDao
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
                socket.emit(Events.USERS_SEARCH, q, page, Ack {
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
        page: Int
    ): Observable<List<User>> {
        return Observable.create { emitter ->
            val cache = if (q.isEmpty())
                userDao.getAllForChat(chatId, page)
            else
                userDao.getByNicknameForChat(q, chatId, page)

            if (cache.isNotEmpty())
                emitter.onNext(cache)

            if (q.isNotEmpty())
                socket.emit(Events.CHATS_USERS_SEARCH, chatId, q, page, Ack {
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

    override fun observeUsersInChat(chatId: Long): Observable<List<User>> {
        return Observable.create { emitter ->
            userDao.getDistinctUserFromChat(chatId)
                .subscribe(
                    { list -> emitter.onNext(list) },
                    { err -> emitter.onError(err) }
                )
        }
    }

    override fun getAndObserveUser(id: Long): Observable<User> {
        return Observable.create { emitter ->

            userDao.observeById(id)
                .subscribe({
                    if (it != null) emitter.onNext(it)
                }, {})

            socket.emit(Events.USERS, id, Ack {
                val user = Gson().fromJson(it[0].toString(), User::class.java)
                userDao.insert(user)
            })
        }
    }

    override fun getUser(id: Long): Observable<User> {
        return Observable.create { emitter ->
            val cache = userDao.getById(id)

            if (cache != null)
                emitter.onNext(cache)

            socket.emit(Events.USERS, id, Ack {
                if (it.isEmpty()) {
                    emitter.onComplete()
                    return@Ack
                }
                val user = Gson().fromJson(it[0].toString(), User::class.java)
                userDao.insert(user)
                emitter.onNext(user)
                emitter.onComplete()
            })
        }
    }

    override fun updateUser(nickname: String, name: String, surname: String): Single<User> {
        return Single.create { emitter ->
            socket.emit(Events.USERS_UPDATE, nickname, name, surname, Ack {
                val user = Gson().fromJson(it[0].toString(), User::class.java)
                userDao.insert(user)
                emitter.onSuccess(user)
            })
        }
    }
}
