package com.tarlad.client.repos

import com.tarlad.client.models.dto.LastMessage
import com.tarlad.client.models.db.RefreshToken
import io.reactivex.rxjava3.core.Observable

interface MainRepo {

    fun getChats(token: String, userId: Long): Observable<List<LastMessage>>
}