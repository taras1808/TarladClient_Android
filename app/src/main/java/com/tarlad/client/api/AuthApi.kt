package com.tarlad.client.api

import com.tarlad.client.models.LoginInfo
import com.tarlad.client.models.Token
import com.tarlad.client.models.User
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.*

interface AuthApi {
    @POST("api/checkEmail")
    fun checkEmail(@Query("email") email: String): Call<Unit>

    @POST("api/register")
    fun register(@Body user: User): Observable<Token>

    @POST("api/loginWithToken")
    fun loginWithToken(@Body token: Token): Observable<Token>

    @POST("api/login")
    fun login(@Body info: LoginInfo): Observable<Token>
}