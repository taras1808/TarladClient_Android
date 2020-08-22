package com.tarlad.client.repos

import com.tarlad.client.models.dto.LoginCredentials
import com.tarlad.client.models.db.RefreshToken
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.models.dto.Token
import io.reactivex.rxjava3.core.Single

interface AuthRepo {
    fun checkEmail(email: String): Single<Unit>
    fun checkNickname(nickname: String): Single<Unit>
    fun register(user: User): Single<Token>
    fun login(loginCredentials: LoginCredentials): Single<Token>
    fun loginWithToken(token: RefreshTokenDTO): Single<Token>
    fun logout(token: RefreshTokenDTO): Single<Unit>
    fun saveToken(token: RefreshToken)
    fun removeToken(token: RefreshToken)
    fun getToken(): RefreshToken?
}
