package com.tarlad.client.repos

import com.tarlad.client.models.db.User
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface UsersRepo {
    fun searchUsers(q: String, userId: Long, page: Int): Observable<List<User>>
    fun searchUsersForChat(q: String, chatId: Long, page: Int): Observable<List<User>>
    fun observeUsersInChat(chatId: Long): Observable<List<User>>
    fun getAndObserveUser(id: Long): Observable<User>
    fun getUser(id: Long): Observable<User>
    fun updateUser(nickname: String, name: String, surname: String): Single<User>
}
