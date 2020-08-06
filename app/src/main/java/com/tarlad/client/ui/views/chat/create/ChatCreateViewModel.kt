package com.tarlad.client.ui.views.chat.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.ChatCreator
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.UsersRepo
import io.reactivex.rxjava3.disposables.Disposable
import java.util.ArrayList

class ChatCreateViewModel(
    private val usersRepo: UsersRepo,
    private val chatsRepo: ChatsRepo,
    private val appSession: AppSession
): ViewModel() {

    val error = MutableLiveData<String>()
    val users = MutableLiveData<List<User>>()

    val openChat = MutableLiveData<Chat>()

    var searchUsersDisposable: Disposable? = null

    var page = -1

    fun search(q: String) {
        val userId = appSession.userId ?: return
        page++
        searchUsersDisposable = usersRepo.searchUsers(q, userId, page)
            .ioMain()
            .subscribe(
                { users.value = it },
                { error.value = it.toString() }
            )
    }

    fun createChat(users: ArrayList<Long>) {
        val token = appSession.token ?: return
        val userId = appSession.userId ?: return
        if (users.size == 0) {
            error.value = "Choose users"
            return
        }
        val chatCreator = ChatCreator(users)
        chatsRepo.createChat(userId, token, chatCreator)
            .ioMain()
            .subscribe(
                { openChat.value = it },
                { error.value = it.toString() }
            )
    }
}

