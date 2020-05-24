package com.tarlad.client

import androidx.lifecycle.MutableLiveData
import com.tarlad.client.models.Token
import com.tarlad.client.states.AppStates

class AppSession {
    val state = MutableLiveData(AppStates.Authenticated)
    var token: Token? = null
}