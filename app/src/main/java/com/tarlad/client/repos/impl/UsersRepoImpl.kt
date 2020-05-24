package com.tarlad.client.repos.impl

import com.tarlad.client.api.UsersApi
import com.tarlad.client.dao.UserDao
import com.tarlad.client.models.User
import com.tarlad.client.repos.UsersRepo
import io.reactivex.rxjava3.core.Observable

class UsersRepoImpl(private val userDao: UserDao, private val usersApi: UsersApi) : UsersRepo {

    override fun searchUsers(q: String, userId: Long): Observable<List<User>> {
        var cache: List<User>? = null
        return Observable
            .fromCallable { if (q.isEmpty()) userDao.getAll(userId) else userDao.getByNickname(q, userId) }
            .concatWith(usersApi.searchUsers(q, userId))
            .filter {
                if (cache == null) {
                    println("cache: $it")
                    cache = it
                } else {
                    println("net: $it")
                    if (cache!!.size != it.size || !cache!!.containsAll(it))
                        saveUsers(cache!!, it)
                    else
                        return@filter false
                }
                true
            }
    }

    private fun saveUsers(cache: List<User>, net: List<User>) {
        val newAndUpdated = ArrayList(net).apply { removeAll(cache) }
        val rest = ArrayList(cache).apply { removeAll(net) }
        userDao.deleteAll(rest)
        userDao.insertOrReplace(newAndUpdated)
    }


}