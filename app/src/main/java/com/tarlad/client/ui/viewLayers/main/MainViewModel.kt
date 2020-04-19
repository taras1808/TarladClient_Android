package com.tarlad.client.ui.viewLayers.main

import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.models.Token
import com.tarlad.client.repos.MainRepo
import com.tarlad.client.states.AppStates

class MainViewModel(val appSession: AppSession, val mainrepo: MainRepo): ViewModel() {
    val token: Token?
        get() = appSession.token

    fun logout() {
        mainrepo.forgetToken()
        appSession.token = null
        appSession.state.value = AppStates.NotAuthenticated
    }
}