package com.tarlad.client

import androidx.lifecycle.MutableLiveData
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.helpers.Preferences
import com.tarlad.client.models.db.RefreshToken
import com.tarlad.client.states.AppStates

class AppSession(private val preferences: Preferences) {
    val state = MutableLiveData(AppStates.Proceed)
    var token: String?
        get() = preferences.token
        set(value) = set(value)

    var userId: Long?
        get() = preferences.userId
        set(value) = set(value)

    private fun set(token: String?) {
        preferences.token = token
    }

    private fun set(userId: Long?) {
        preferences.userId = userId
    }
}