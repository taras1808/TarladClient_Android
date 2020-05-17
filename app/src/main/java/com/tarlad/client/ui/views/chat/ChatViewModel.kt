package com.tarlad.client.ui.views.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.helpers.OnComplete
import com.tarlad.client.helpers.OnError
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.*
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.MessagesRepo
import com.tarlad.client.repos.UsersRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ChatViewModel(
    private val usersRepo: UsersRepo,
    private val chatsRepo: ChatsRepo,
    private val messagesRepo: MessagesRepo,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
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
        CoroutineScope(Dispatchers.IO).launch {
            when (val res = messagesRepo.sendMessage(messageCreator)) {
                is OnComplete -> {
                    messageDao.insert(res.data)
                    loadMessages(chatId)
                }
                is OnError -> error.postValue(res.t.toString())
            }
        }
    }

    fun loadMessages(chatId: Long){
        CoroutineScope(Dispatchers.IO).launch {
            messages.postValue(messageDao.getMessagesForChat(chatId))
//            when (val res = messagesRepo.sendMessage(messageCreator)) {
//                is OnComplete -> {
//                    messageDao.insert(res.data)
//                }
//                is OnError -> error.postValue(res.t.toString())
//            }
        }
    }
}

