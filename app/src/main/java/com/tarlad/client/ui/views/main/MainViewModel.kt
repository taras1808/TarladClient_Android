package com.tarlad.client.ui.views.main

import android.view.MenuItem
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.R
import com.tarlad.client.enums.Chats
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.repos.*
import com.tarlad.client.states.AppStates
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.socket.client.Socket
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainViewModel(
    private val socket: Socket,
    val appSession: AppSession,
    private val mainRepo: MainRepo,
    private val authRepo: AuthRepo,
    private val usersRepo: UsersRepo,
    private val chatsRepo: ChatsRepo,
    private val imageRepo: ImageRepo
): ViewModel() {

    val fragment = MutableLiveData(0)

    val toolbarTitle = MutableLiveData<String>()

    val messages: ArrayList<Message> = arrayListOf()
    val users: ArrayList<User> = arrayListOf()
    val chats: ArrayList<Chat> = arrayListOf()
    val chatLists: HashMap<Long, List<Long>> = hashMapOf()

    val fullName = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()

    val error = MutableLiveData<String>()
    val messagesLiveData = MutableLiveData<ArrayList<Pair<Chats, List<Message>>>>(arrayListOf())
    val usersLiveDate = MutableLiveData<User>()
    val chatsLiveDate = MutableLiveData<Chat>()
    val chatListsLiveDate = MutableLiveData<Pair<Long, List<User>>>()
    val openChat = MutableLiveData<Long>()

    var page = 0L
    var time = Date().time

    fun getMessages() {
        mainRepo.getMessages(time, page++)
            .ioMain()
            .subscribe(
                {
                    messagesLiveData.value!!.add(it)
                    messagesLiveData.value = messagesLiveData.value
                },
                { error.value = it.toString() }
            )
    }

    fun getUser(userId: Long) {
        usersRepo.getUser(userId)
            .ioMain()
            .subscribe(
                { usersLiveDate.value = it },
                { error.value = it.toString() }
            )
    }

    fun getChat(chatId: Long) {
        chatsRepo.getChat(chatId)
            .ioMain()
            .subscribe(
                { chatsLiveDate.value = it },
                { error.value = it.toString() }
            )
    }

    fun getChatLists(chatId: Long) {
        chatsRepo.getChatLists(chatId)
            .ioMain()
            .subscribe(
                { chatListsLiveDate.value = Pair(chatId, it) },
                { error.value = it.toString() }
            )
    }

    fun observeMessages() {
        mainRepo.observeMessages()
            .ioMain()
            .subscribe(
                {
                    messagesLiveData.value!!.add(it)
                    messagesLiveData.value = messagesLiveData.value
                },
                { error.value = it.toString() }
            )
    }

    fun observeChats() {
        chatsRepo.observeChats()
            .ioMain()
            .subscribe(
                {
                    getChat(it)
                    getChatLists(it)
                },
                { error.value = it.toString() }
            )
    }

    fun logout() {
        Single.fromCallable { authRepo.getToken() }
            .ioMain()
            .subscribe(
                { refreshToken ->
                    if (refreshToken == null) {
                        appSession.state.value = AppStates.NotAuthenticated
                        return@subscribe
                    }
                    authRepo.logout(RefreshTokenDTO(refreshToken.value))
                        .doOnSuccess {
                            authRepo.removeToken(refreshToken)
                        }
                        .ioMain()
                        .doOnTerminate {
                            appSession.token = null
                            appSession.userId = null
                            appSession.state.value = AppStates.NotAuthenticated
                            socket.disconnect()
                        }
                        .subscribe({}, {})
                }, {})
    }


    fun onNavigationClick(item: MenuItem): Boolean{
        when (item.itemId) {
            R.id.home -> fragment.value = 0
            R.id.profile -> fragment.value = 1
            else -> return false
        }
        return true
    }

    fun loadProfile(): Disposable? {
        val id = appSession.userId ?: return null
        return usersRepo.getAndObserveUser(id)
            .ioMain()
            .subscribe(
                {
                    toolbarTitle.value = it.nickname
                    fullName.value = "${it.name} ${it.surname}"
                    imageUrl.value = it.imageURL
                },
                { error.value = it.toString() }
            )
    }

    fun sendImage(ext: String, data: ByteArray) {
        imageRepo.saveImage(ext, data)
    }

    fun removeImage() {
        imageRepo.removeImage()
    }
}
