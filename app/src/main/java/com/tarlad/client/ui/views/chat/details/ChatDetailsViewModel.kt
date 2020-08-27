package com.tarlad.client.ui.views.chat.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.UsersRepo

class ChatDetailsViewModel(
    val appSession: AppSession,
    private val usersRepo: UsersRepo
): ViewModel() {

    val title = MutableLiveData<String>()

    val error = MutableLiveData<String>()
    val users = MutableLiveData<List<User>>()

    fun loadUsers(chatId: Long){
        usersRepo.getUsersFromChat(chatId)
            .ioMain()
            .subscribe(
                { users.value = it },
                { error.value = it.toString() }
            )
    }
}
