package com.tarlad.client.ui.views.main

import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.R
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.LastMessage
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.repos.MainRepo
import com.tarlad.client.states.AppStates
import com.tarlad.client.ui.adapters.MainAdapter
import io.reactivex.rxjava3.core.Single
import io.socket.client.Socket

class MainViewModel(
    private val socket: Socket,
    val appSession: AppSession,
    private val mainRepo: MainRepo,
    private val authRepo: AuthRepo
): ViewModel() {

    val fragment = MutableLiveData<Int>(0)

    val title = MutableLiveData<String>()

    val error = MutableLiveData<String>()
    val messages = MutableLiveData<List<LastMessage>>()
    val openChat = MutableLiveData<Chat>()

    init {
        loadChats()
    }

    fun loadChats() {
        val token = appSession.token ?: return
        val userId = appSession.userId ?: return
        mainRepo.getChats(token, userId)
            .ioMain()
            .subscribe(
                { messages.value = it },
                { error.value = it.toString() }
            )
    }

    fun logout() {
        Single.fromCallable { authRepo.getToken() }
            .ioMain()
            .subscribe(
                { refreshToken ->
                    if (refreshToken == null) {
                        appSession.state.value = AppStates.NotAuthenticated
                        return@subscribe
                    }
                    authRepo.logout(RefreshTokenDTO(refreshToken.value))
                        .doOnSuccess { authRepo.removeToken(refreshToken) }
                        .ioMain()
                        .doOnTerminate {
                            appSession.token = null
                            appSession.userId = null
                            appSession.state.value = AppStates.NotAuthenticated
                            socket.disconnect()
                        }
                        .subscribe({}, {})
                }, {})
    }


    fun onNavigationClick(item: MenuItem): Boolean{
        when (item.itemId) {
            R.id.home -> fragment.value = 0
            R.id.profile -> fragment.value = 1
            else -> return false
        }
        return true
    }
}