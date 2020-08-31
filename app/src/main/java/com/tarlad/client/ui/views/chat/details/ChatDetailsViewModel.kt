package com.tarlad.client.ui.views.chat.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.UsersRepo

class ChatDetailsViewModel(
    val appSession: AppSession,
    private val usersRepo: UsersRepo,
    private val chatsRepo: ChatsRepo
): ViewModel() {

    val title = MutableLiveData<String>()

    val error = MutableLiveData<String>()
    val users = MutableLiveData<List<User>>()


    val admin = MutableLiveData<Long>()

    fun loadUsers(chatId: Long) {
        val userId = appSession.userId ?: return
        usersRepo.getAllUsersFromChat(chatId, userId)
            .ioMain()
            .subscribe(
                { users.value = it },
                { error.value = it.toString() }
            )
    }

    fun loadAdmin(chatId: Long) {
        chatsRepo.getAdminFromChat(chatId)
            .ioMain()
            .subscribe(
                { admin.value = it },
                { error.value = it.toString() }
            )
    }

    fun removeParticipant(chatId: Long, userId: Long) {
        chatsRepo.removeParticipant(chatId, userId)
    }
}
