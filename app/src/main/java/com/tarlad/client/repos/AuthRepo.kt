package com.tarlad.client.repos

import com.tarlad.client.helpers.TarladResult
import com.tarlad.client.models.LoginInfo
import com.tarlad.client.models.Token
import com.tarlad.client.models.User

interface AuthRepo {
    suspend fun checkEmail(email: String): TarladResult<Unit>
    suspend fun checkNickname(nickname: String): TarladResult<Unit>
    suspend fun register(user: User): TarladResult<Token>
    suspend fun login(loginInfo: LoginInfo): TarladResult<Token>
    suspend fun loginWithToken(token: Token): TarladResult<Token>
    suspend fun logout(token: Token): TarladResult<Unit>
    suspend fun saveToken(token: Token)
    suspend fun removeToken(token: Token)
    suspend fun getToken(): Token?
}