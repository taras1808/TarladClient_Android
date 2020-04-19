package com.tarlad.client.repos.impl

import com.tarlad.client.api.UsersApi
import com.tarlad.client.dao.UserDao
import com.tarlad.client.models.Chat
import com.tarlad.client.models.User
import com.tarlad.client.repos.UsersRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class UsersRepoImpl(private val dbUsers: UserDao, private val usersApi: UsersApi) : UsersRepo {
    override fun getUsers(): Observable<List<User>> {
        return Observable.create { emitter ->
            val cache = dbUsers.getAll()
            emitter.onNext(cache)
            usersApi.getUsers()
                .subscribe(
                    { net ->
                        if (net != null && cache.size != net.size || !cache.containsAll(net)) {
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
        dbUsers.deleteAll(rest)
        dbUsers.insertOrReplace(newAndUpdated)
    }


    override fun createChat(users: List<User>): Single<Chat> {
        //TODO save in db

        TODO("send on server")
    }
}