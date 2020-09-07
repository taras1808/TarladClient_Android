package com.tarlad.client.repos

import com.tarlad.client.enums.Chats
import com.tarlad.client.models.db.Message
import io.reactivex.rxjava3.core.Observable

interface MainRepo {
    fun getMessages(time: Long, page: Long): Observable<Pair<Chats, List<Message>>>
    fun observeMessages(): Observable<Pair<Chats, List<Message>>>
    fun truncate()
}
