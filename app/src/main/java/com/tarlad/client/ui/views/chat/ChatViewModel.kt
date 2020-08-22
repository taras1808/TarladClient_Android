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
    val messages = MutableLiveData<Pair<Messages, List<Message>>>()
    val users = MutableLiveData<List<User>>()
    val time = Date().time

    val title = MutableLiveData<String>()

    val message = MutableLiveData<String>()
    var chatId: Long = -1


    var page = 0L

    fun sendMessage(){
        if (message.value.isNullOrEmpty()) return
        val userId = appSession.userId ?: return
        val messageCreator = MessageCreator(
            chatId,
            "text",
            message.value!!.trim(),
            Date().time
        )

        message.value = ""

        messagesRepo.sendMessage(messageCreator, userId)
            .ioMain()
            .subscribe(
                { messages.value = it },
                { error.value = it.toString() }
            )
    }

    fun getMessages(chatId: Long){
        messagesRepo.getMessagesForChatBeforeTime(chatId, time, page++)
            .ioMain()
            .subscribe(
                { messages.value = it },
                { error.value = it.toString() }
            )
    }

    fun observeMessages(chatId: Long) {
        val userId = appSession.userId ?: return
        messagesRepo.observeMessages(chatId, userId)
            .ioMain()
            .subscribe(
                { messages.value = it },
                { error.value = it.toString() }
            )
    }

    fun getUsers(chatId: Long){
        usersRepo.getUsersFromChat(chatId)
            .ioMain()
            .subscribe(
                { users.value = it },
                { error.value = it.toString() }
            )
    }

    fun deleteMessage(id: Long) {
        messagesRepo.deleteMessage(id)
            .ioMain()
            .subscribe(
                { messages.value = it },
                { error.value = it.toString() }
            )
    }
}