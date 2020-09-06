package com.tarlad.client.repos

import com.tarlad.client.models.db.User
import io.reactivex.rxjava3.core.Observable

interface UsersRepo {
    fun searchUsers(q: String, userId: Long, page: Int): Observable<List<User>>
    fun searchUsersForChat(q: String, chatId: Long, page: Int): Observable<List<User>>
    fun getUsersFromChat(chatId: Long): Observable<List<User>>
    fun loadProfile(id: Long): Observable<User>
}
