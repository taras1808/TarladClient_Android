package com.tarlad.client.repos.impl

import com.tarlad.client.AppDatabase
import com.tarlad.client.api.AuthApi
import com.tarlad.client.helpers.Preferences
import com.tarlad.client.models.LoginInfo
import com.tarlad.client.models.Token
import com.tarlad.client.models.User
import com.tarlad.client.repos.AuthRepo
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepoImpl(val prefs: Preferences, val db: AppDatabase, val authApi: AuthApi) : AuthRepo {

    //return true when email exists
    override suspend fun checkEmail(email: String): Boolean {
        return suspendCoroutine {
            authApi.checkEmail(email).enqueue(object : Callback<Unit> {
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    it.resume(true)
                }

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.code() == 200) {
                        it.resume(false)
                    } else {
                        it.resume(true)
                    }
                }
            })
        }
    }

    override fun register(user: User): Observable<Token> = authApi.register(user)

    override fun saveToken(token: Token) {
        prefs.token = token.token
        prefs.idUser = token.idUser
    }

    override fun loginWithToken(token: Token): Observable<Token> = authApi.loginWithToken(token)

    override fun login(loginInfo: LoginInfo): Observable<Token> = authApi.login(loginInfo)
}