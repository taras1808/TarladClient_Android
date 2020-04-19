package com.tarlad.client.ui.viewLayers.addChat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.User
import com.tarlad.client.repos.UsersRepo
import java.util.ArrayList

class AddChatViewModel(private val usersRepo: UsersRepo): ViewModel() {
    val refreshing = MutableLiveData(false)
    val error = MutableLiveData<String>()
    val users = MutableLiveData<List<User>>()

    fun refresh() {
        refreshing.value = true
        usersRepo.getUsers()
            .ioMain()
            .doOnComplete {
                refreshing.value = false
            }
            .subscribe(
                {
                    users.value = it
                },
                {
                    error.value = it.toString()
                    refreshing.value = false
                }
            )
    }

    fun createChat(users: ArrayList<User>) {

    }
}

