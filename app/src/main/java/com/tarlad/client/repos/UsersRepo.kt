package com.tarlad.client.repos

import com.tarlad.client.models.Chat
import com.tarlad.client.models.User
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface UsersRepo {
    fun searchUsers(q: String, userId: Long): Observable<List<User>>
}