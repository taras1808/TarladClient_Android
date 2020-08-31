package com.tarlad.client.ui.views.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.dto.MessageCreator
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.MessagesRepo
import com.tarlad.client.repos.UsersRepo
import java.util.*
import kotlin.collections.ArrayList

class ChatViewModel(
    val appSession: AppSession,
    private val usersRepo: UsersRepo,
    private val messagesRepo: MessagesRepo
) : ViewModel() {

    val error = MutableLiveData<String>()
    val messages = MutableLiveData<ArrayList<Pair<Messages, List<Message>>>>(arrayListOf())
    val users = MutableLiveData<List<User>>()
    val time = Date().time

    val title = MutableLiveData<String>()

    val isEdit = MutableLiveData<Boolean>(false)

    val message = MutableLiveData<String>()
    val editMessage = MutableLiveData<Message>()
    var chatId: Long = -1

    var page = 0L

    fun sendMessage() {
        if (message.value.isNullOrEmpty()) return
        val userId = appSession.userId ?: return
        val message = Message(
            -1,
            chatId,
            userId,
            "text",
            message.value!!.trim(),
            Date().time
        )

        this.message.value = ""

        messagesRepo.sendMessage(message, userId)
            .ioMain()
            .subscribe(
                {
                    messages.value!!.add(it)
                    messages.value = messages.value
                },
                { error.value = it.toString() }
            )
    }

    fun getMessages(chatId: Long) {
        messagesRepo.getMessagesForChatBeforeTime(chatId, time, page++)
            .ioMain()
            .subscribe(
                {
                    messages.value!!.add(it)
                    messages.value = messages.value
                },
                { error.value = it.toString() }
            )
    }

    fun observeMessages(chatId: Long) {
        val userId = appSession.userId ?: return
        messagesRepo.observeMessages(chatId, userId)
            .ioMain()
            .subscribe(
                {
                    messages.value!!.add(it)
                    messages.value = messages.value
                },
                { error.value = it.toString() }
            )
    }

    fun getUsers(chatId: Long) {
        usersRepo.getUsersFromChat(chatId)
            .ioMain()
            .subscribe(
                { users.value = it },
                { error.value = it.toString() }
            )
    }

    fun deleteMessage(message: Message) {
        messagesRepo.deleteMessage(message)
            .ioMain()
            .subscribe(
                {
                    messages.value!!.add(it)
                    messages.value = messages.value
                },
                { error.value = it.toString() }
            )
    }

    fun editMessage(message: Message) {
        this.message.value = message.data
        editMessage.value = message
        isEdit.value = true
    }

    fun editMessage() {
        if (editMessage.value == null) return
        val mes = editMessage.value!!

        if (message.value.isNullOrEmpty()) {
            deleteMessage(mes)
            stopEditing()
            return
        }

        if (message.value == mes.data) {
            stopEditing()
            return
        }

        messagesRepo.editMessage(mes, Message(mes.id, mes.chatId, mes.userId, mes.type, message.value!!, mes.time))
            .ioMain()
            .subscribe(
                {
                    messages.value!!.add(it)
                    messages.value = messages.value
                    stopEditing()
                },
                { error.value = it.toString() }
            )
    }

    fun stopEditing() {
        editMessage.value = null
        isEdit.value = false
        message.value = ""
    }
}
