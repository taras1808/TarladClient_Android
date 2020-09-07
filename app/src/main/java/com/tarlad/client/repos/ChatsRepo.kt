package com.tarlad.client.repos

import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.User
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface ChatsRepo {
    fun createChat(data: List<Long>): Single<Chat>
    fun addParticipants(chatId: Long, data: List<Long>): Single<Unit>
    fun getAdminFromChat(chatId: Long): Single<Long>
    fun removeParticipant(chatId: Long, userId: Long)
    fun getTitle(chatId: Long): Single<String>
    fun saveTitle(chatId: Long, title: String): Single<Unit>
    fun getChat(id: Long): Observable<Chat>
    fun getChatLists(id: Long): Observable<List<User>>
    fun observeChats(): Observable<Long>
    fun observeChat(chatId: Long): Observable<Chat>
}
