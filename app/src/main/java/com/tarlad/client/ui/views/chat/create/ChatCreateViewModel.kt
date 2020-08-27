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

    val title = MutableLiveData<String>()

    val search = MutableLiveData<String>()

    val complete = MutableLiveData<Boolean>(false)

    val error = MutableLiveData<String>()
    val users = MutableLiveData<List<User>>()

    val openChat = MutableLiveData<Chat>()

    var searchUsersDisposable: Disposable? = null

    var page = 0

    fun search() {
        val userId = appSession.userId ?: return
        searchUsersDisposable = usersRepo.searchUsers(search.value ?: "", userId, page++)
            .ioMain()
            .subscribe(
                { users.value = it },
                { error.value = it.toString() },
                { complete.value = true }
            )
    }

    fun createChat(users: ArrayList<Long>) {
        if (users.size == 0) {
            error.value = "Choose users"
            return
        }
        val chatCreator = ChatCreator(users)
        chatsRepo.createChat(chatCreator)
            .ioMain()
            .subscribe(
                { openChat.value = it },
                { error.value = it.toString() }
            )
    }
}
