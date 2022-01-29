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

    val chatTitle = MutableLiveData<String>()
    val chatTitleSaved = MutableLiveData<String>()

    val error = MutableLiveData<String>()
    val users = MutableLiveData<List<User>>()

    val admin = MutableLiveData<Long>()

    fun loadChatTitle(chatId: Long) {
        chatsRepo.getTitle(chatId)
            .ioMain()
            .subscribe(
                {
                    chatTitle.value = it
                    chatTitleSaved.value = it
                },
                { error.value = it.toString() }
            )
    }

    fun loadUsers(chatId: Long) {
        usersRepo.observeUsersInChat(chatId)
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

    fun changeTitle(chatId: Long) {
        chatsRepo.saveTitle(chatId, chatTitle.value ?: "")
            .ioMain()
            .subscribe(
                { chatTitleSaved.value = chatTitle.value },
                { error.value = it.toString() }
            )
    }

    fun leaveChat(chatId: Long) {
        chatsRepo.removeParticipant(chatId, appSession.userId!!)
    }
}
