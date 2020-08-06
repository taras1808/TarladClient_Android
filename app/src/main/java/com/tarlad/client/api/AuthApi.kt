package com.tarlad.client.api

import com.tarlad.client.models.dto.LoginCredentials
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.models.dto.Token
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface AuthApi {

    @GET("api/accounts/check-email")
    fun checkEmail(@Query("email") email: String): Single<Unit>

    @GET("api/accounts/check-nickname")
    fun checkNickname(@Query("nickname") nickname: String): Single<Unit>

    @POST("api/accounts/register")
    fun register(@Body user: User): Single<Token>

    @POST("api/accounts/authorize")
    fun login(@Body loginCredentials: LoginCredentials): Single<Token>

    @POST("api/accounts/authenticate")
    fun loginWithToken(@Body token: RefreshTokenDTO): Single<Token>

    @POST("api/accounts/logout")
    fun logout(@Body token: RefreshTokenDTO): Single<Unit>
}