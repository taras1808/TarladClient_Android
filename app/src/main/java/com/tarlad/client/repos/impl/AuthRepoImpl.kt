package com.tarlad.client.repos.impl

import com.tarlad.client.api.AuthApi
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.helpers.*
import com.tarlad.client.models.LoginInfo
import com.tarlad.client.models.Token
import com.tarlad.client.models.User
import com.tarlad.client.repos.AuthRepo

class AuthRepoImpl(private val tokenDao: TokenDao, private val authApi: AuthApi) : AuthRepo {

    override suspend fun checkEmail(email: String): TarladResult<Unit>
            = authApi.checkEmail(email).toCoroutine()

    override suspend fun checkNickname(nickname: String): TarladResult<Unit>
            = authApi.checkNickname(nickname).toCoroutine()

    override suspend fun register(user: User): TarladResult<Token>
            = authApi.register(user).toCoroutine()

    override suspend fun login(loginInfo: LoginInfo): TarladResult<Token>
            = authApi.login(loginInfo).toCoroutine()

    override suspend fun loginWithToken(token: Token): TarladResult<Token>
            = authApi.loginWithToken(token).toCoroutine()

    override suspend fun logout(token: Token): TarladResult<Unit>
            = authApi.logout(token).toCoroutine()

    override suspend fun saveToken(token: Token)
            = tokenDao.insert(token)

    override suspend fun removeToken(token: Token)
            = tokenDao.delete(token)

    override suspend fun getToken(): Token?
            = tokenDao.getToken()
}