package com.tarlad.client.repos.impl

import com.tarlad.client.api.AuthApi
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.models.db.RefreshToken
import com.tarlad.client.models.dto.LoginCredentials
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.models.dto.Token
import com.tarlad.client.repos.AuthRepo
import io.reactivex.rxjava3.core.Single

class AuthRepoImpl(private val tokenDao: TokenDao, private val authApi: AuthApi) : AuthRepo {

    override fun checkEmail(email: String): Single<Unit> {
        return authApi.checkEmail(email)
    }

    override fun checkNickname(nickname: String): Single<Unit> {
        return authApi.checkNickname(nickname)
    }

    override fun register(user: User): Single<Token> {
        return authApi.register(user)
    }

    override fun login(loginCredentials: LoginCredentials): Single<Token> {
        return authApi.login(loginCredentials)
    }

    override fun loginWithToken(token: RefreshTokenDTO): Single<Token> {
        return authApi.loginWithToken(token)
    }

    override fun logout(token: RefreshTokenDTO): Single<Unit> {
        return authApi.logout(token)
    }

    override fun saveToken(token: RefreshToken) {
        tokenDao.insert(token)
    }

    override fun removeToken(token: RefreshToken) {
        tokenDao.delete(token)
    }

    override fun getToken(): RefreshToken? {
        return tokenDao.getToken()
    }

}
