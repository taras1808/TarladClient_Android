package com.tarlad.client.repos

import com.tarlad.client.models.dto.LastMessage
import com.tarlad.client.ui.views.main.Chats
import io.reactivex.rxjava3.core.Observable

interface MainRepo {
    fun getChats(time: Long, page: Long): Observable<Pair<Chats, List<LastMessage>>>
    fun observeChats(): Observable<Pair<Chats, List<LastMessage>>>
    fun truncate()
}
