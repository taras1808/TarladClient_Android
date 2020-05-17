package com.tarlad.client.repos.impl

import com.tarlad.client.api.UsersApi
import com.tarlad.client.dao.UserDao
import com.tarlad.client.helpers.TarladResult
import com.tarlad.client.models.Chat
import com.tarlad.client.models.User
import com.tarlad.client.repos.UsersRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class UsersRepoImpl(private val userDao: UserDao, private val usersApi: UsersApi) : UsersRepo {
    override fun searchUsers(q: String, userId: Long): Observable<List<User>> {
        return Observable.create { emitter ->
            val cache =
                if (q.isEmpty()) userDao.getAll(userId)
                else userDao.getByNickname(q, userId)
            emitter.onNext(cache)
            usersApi.searchUsers(q, userId)
                .subscribe(
                    { net ->
                        if (cache.size != net.size || !cache.containsAll(net)) {
                            saveUsers(cache, net)
                            emitter.onNext(net)
                        }
                        emitter.onComplete()
                    },
                    {
                        emitter.onError(it)
                        emitter.onComplete()
                    }
                )
        }
    }

    private fun saveUsers(cache: List<User>, net: List<User>) {
        val newAndUpdated = ArrayList(net).apply { removeAll(cache) }
        val rest = ArrayList(cache).apply { removeAll(net) }
        userDao.deleteAll(rest)
        userDao.insertOrReplace(newAndUpdated)
    }


}