package com.tarlad.client.ui.views.main

import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.OnComplete
import com.tarlad.client.helpers.OnError
import com.tarlad.client.models.Token
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.repos.MainRepo
import com.tarlad.client.states.AppStates
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.ConnectException

class MainViewModel(val appSession: AppSession, private val mainRepo: MainRepo, private val authRepo: AuthRepo): ViewModel() {
    val token: Token?
        get() = appSession.token

    fun tryLoginWithToken() {
        CoroutineScope(Dispatchers.Main).launch {
            val token = withContext(Dispatchers.IO) { authRepo.getToken() }
            if (token == null) {
                appSession.state.value = AppStates.NotAuthenticated
                return@launch
            }
            when (val res = withContext(Dispatchers.IO) { authRepo.loginWithToken(token) }){
                is OnComplete -> {
                    appSession.token = res.data
                    withContext(Dispatchers.IO) {
                        authRepo.removeToken(token)
                        authRepo.saveToken(res.data)
                    }
                }
                is OnError -> {
                    when (res.t) {
                        is ConnectException -> {
                            appSession.token = token
                        }
                        else -> {
                            appSession.state.value = AppStates.NotAuthenticated
                            withContext(Dispatchers.IO) { authRepo.removeToken(token) }
                        }
                    }
                }
            }
        }
    }

    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            when (val res = authRepo.logout(token!!)) {
                is OnComplete -> {
                    authRepo.removeToken(token!!)
                }
                is OnError -> {
                    // TODO
                }
            }
            appSession.token = null
            appSession.state.postValue(AppStates.NotAuthenticated)
        }
    }
}