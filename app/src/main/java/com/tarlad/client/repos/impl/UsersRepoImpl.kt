package com.tarlad.client.repos.impl

import com.tarlad.client.api.UsersApi
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.dao.UserDao
import com.tarlad.client.helpers.ioIo
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.UsersRepo
import io.reactivex.rxjava3.core.Observable

class UsersRepoImpl(
    private val userDao: UserDao,
    private val usersApi: UsersApi
) : UsersRepo {

    override fun searchUsers(q: String, userId: Long, page: Int): Observable<List<User>> {
        return Observable.create {
            val cache = if (q.isEmpty()) userDao.getAll(userId, page) else userDao.getByNickname(
                q,
                userId,
                page
            )
            it.onNext(cache)
            if (q.isNotEmpty())
                usersApi.searchUsers(q, userId, page)
                    .ioIo()
                    .subscribe(
                        { users ->
                            if (cache.size != users.size || !cache.containsAll(users)) {
                                it.onNext(users)
                                userDao.insertAll(users)
                            }
                            it.onComplete()
                        },
                        { err -> it.onError(err) }
                    )
            else it.onComplete()
        }
    }

    override fun searchUsersForChat(
        token: String,
        q: String,
        chatId: Long,
        page: Int
    ): Observable<List<User>> {
        return Observable.create {
            val cache = if (q.isEmpty()) userDao.getAllForChat(
                chatId,
                page
            ) else userDao.getByNicknameForChat(q, chatId, page)
            it.onNext(cache)
            if (q.isNotEmpty())
                usersApi.searchUsersForChat("Bearer $token", chatId, q, page)
                    .ioIo()
                    .subscribe(
                        { users ->
                            if (cache.size != users.size || !cache.containsAll(users)) {
                                it.onNext(users)
                                userDao.insertAll(users)
                            }
                            it.onComplete()
                        },
                        { err -> it.onError(err) }
                    )
            else it.onComplete()
        }
    }

    override fun getUsersFromChat(chatId: Long): Observable<List<User>> {
        return Observable.create { emitter ->
            userDao.getDistinctUserFromChat(chatId)
                .subscribe({ emitter.onNext(it) }, {})
        }
    }
}
