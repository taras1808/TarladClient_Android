package com.tarlad.client.repos

import com.tarlad.client.models.LoginInfo
import com.tarlad.client.models.Token
import com.tarlad.client.models.User
import io.reactivex.rxjava3.core.Observable

interface AuthRepo {
    suspend fun checkEmail(email: String): Boolean
    fun register(user: User): Observable<Token>
    fun saveToken(token: Token)

    fun loginWithToken(token: Token): Observable<Token>
    fun login(loginInfo: LoginInfo): Observable<Token>
}