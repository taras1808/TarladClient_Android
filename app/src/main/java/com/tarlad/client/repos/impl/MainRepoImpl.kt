package com.tarlad.client.repos.impl

import com.tarlad.client.helpers.Preferences
import com.tarlad.client.repos.MainRepo

class MainRepoImpl(private val prefs: Preferences): MainRepo {
    override fun forgetToken() {
        prefs.idUser = -1
        prefs.token = ""
    }
}