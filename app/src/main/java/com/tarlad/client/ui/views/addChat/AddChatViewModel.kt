package com.tarlad.client.ui.views.addChat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.helpers.OnComplete
import com.tarlad.client.helpers.OnError
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.Chat
import com.tarlad.client.models.ChatCreator
import com.tarlad.client.models.User
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.UsersRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class AddChatViewModel(
    private val usersRepo: UsersRepo,
    private val chatsRepo: ChatsRepo,
    private val chatDao: ChatDao,
    private val appSession: AppSession
): ViewModel() {

    val error = MutableLiveData<String>()
    val users = MutableLiveData<List<User>>()

    val openChat = MutableLiveData<Chat>()

    fun search(q: String) {
        usersRepo.searchUsers(q, appSession.token!!.userId)
            .ioMain()
            .subscribe(
                {
                    println(it)
                    users.value = it },
                { error.value = it.toString() }
            )
    }

    fun createChat(users: ArrayList<Long>) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val res = chatsRepo.createChat(ChatCreator(appSession.token!!,users))) {
                is OnComplete -> {
                    openChat.postValue(res.data)
                    chatDao.insert(res.data)
                }
                is OnError -> error.postValue(res.t.toString())
            }
        }
    }
}

