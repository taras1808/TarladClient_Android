package com.tarlad.client.ui.views.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.helpers.ioIo
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.Message
import com.tarlad.client.models.MessageCreator
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.MessagesRepo
import com.tarlad.client.repos.UsersRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ChatViewModel(
    private val usersRepo: UsersRepo,
    private val chatsRepo: ChatsRepo,
    private val messagesRepo: MessagesRepo,
    private val appSession: AppSession
): ViewModel() {

    val error = MutableLiveData<String>()
    val messages = MutableLiveData<List<Message>>()

    fun sendMessage(text: String, chatId: Long){
        val messageCreator = MessageCreator(
            appSession.token,
            chatId,
            "text",
            text,
            Date().time
        )

        messagesRepo.sendMessage(messageCreator)
            .ioMain()
            .subscribe(
                {
                    loadMessages(chatId)
                },
                {
                    error.value = it.toString()
                })
    }

    fun loadMessages(chatId: Long){
        messagesRepo.getMessagesForChat(chatId)
            .ioMain()
            .subscribe(
                {
                    messages.value = it
                },
                {
                    error.value = it.toString()
                })
    }
}

