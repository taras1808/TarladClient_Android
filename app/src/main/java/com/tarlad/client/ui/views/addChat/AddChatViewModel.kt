package com.tarlad.client.ui.views.addChat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.OnComplete
import com.tarlad.client.helpers.OnError
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.User
import com.tarlad.client.repos.UsersRepo
import com.tarlad.client.states.AppStates
import com.tarlad.client.states.Register
import kotlinx.coroutines.*
import java.util.ArrayList

class AddChatViewModel(private val usersRepo: UsersRepo, private val appSession: AppSession): ViewModel() {
    val error = MutableLiveData<String>()
    val users = MutableLiveData<List<User>>()

    fun search(q: String) {
        usersRepo.searchUsers(q, appSession.token!!.userId)
            .ioMain()
            .subscribe(
                { users.value = it },
                { error.value = it.toString() }
            )
    }

    fun createChat(users: ArrayList<User>) {

    }
}

