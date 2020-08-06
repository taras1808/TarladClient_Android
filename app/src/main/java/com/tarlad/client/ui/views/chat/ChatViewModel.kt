package com.tarlad.client.ui.views.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.dto.MessageCreator
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.MessagesRepo
import com.tarlad.client.repos.UsersRepo
import java.util.*

class ChatViewModel(
    val appSession: AppSession,
    private val usersRepo: UsersRepo,
    private val chatsRepo: ChatsRepo,
    private val messagesRepo: MessagesRepo
): ViewModel() {

    val error = MutableLiveData<String>()
    val messages = MutableLiveData<Pair<String, List<Message>>>()
    val users = MutableLiveData<List<User>>()
    val complete = MutableLiveData<Boolean>(false)

    fun sendMessage(text: String, chatId: Long){
        val messageCreator = MessageCreator(
            chatId,
            "text",
            text,
            Date().time
        )

        messagesRepo.sendMessage(messageCreator)
    }

    fun loadOldMessages(chatId: Long, time: Long){
        println(time)
        val token = appSession.token ?: return
        val id = UUID.randomUUID().toString()
        messagesRepo.getMessagesForChatBeforeTime(token, chatId, time)
            .ioMain()
            .subscribe(
                { messages.value = Pair(id, it) },
                { error.value = it.toString() },
                { complete.value = true }
            )
    }

    fun loadNewMessages(chatId: Long, time: Long){
        val token = appSession.token ?: return
        val id = UUID.randomUUID().toString()
        messagesRepo.getMessagesForChatAfterTime(token, chatId, time)
            .ioMain()
            .subscribe(
                { messages.value = Pair(id, it) },
                { error.value = it.toString() }
            )
    }

    fun loadUsers(chatId: Long){
        usersRepo.getUsersFromChat(chatId)
            .ioMain()
            .subscribe(
                { users.value = it },
                { error.value = it.toString() }
            )
    }
}

