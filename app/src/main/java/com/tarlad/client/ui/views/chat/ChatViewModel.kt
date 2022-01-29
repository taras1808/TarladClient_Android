package com.tarlad.client.ui.views.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.enums.Messages
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.ImageRepo
import com.tarlad.client.repos.MessagesRepo
import com.tarlad.client.repos.UsersRepo
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ChatViewModel(
    val appSession: AppSession,
    private val usersRepo: UsersRepo,
    private val messagesRepo: MessagesRepo,
    private val chatsRepo: ChatsRepo,
    private val imageRepo: ImageRepo
) : ViewModel() {

    val messages: ArrayList<Message> = arrayListOf()
    val users: ArrayList<User> = arrayListOf()

    val error = MutableLiveData<String>()
    val messagesLiveData = MutableLiveData<ArrayList<Pair<Messages, List<Message>>>>(arrayListOf())
    val usersLiveData = MutableLiveData<List<User>>(arrayListOf())
    val userLiveData = MutableLiveData<User>()
    val chatLiveData = MutableLiveData<Chat>()

    val title = MutableLiveData<String>()

    val image = MutableLiveData<String>("")

    val isEdit = MutableLiveData<Boolean>(false)

    val message = MutableLiveData<String>()
    val editMessage = MutableLiveData<Message?>(null)
    var chatId: Long = -1

    init {
        messagesRepo.time = Long.MAX_VALUE
    }


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

        messagesRepo.sendMessage(message)
            .ioMain()
            .subscribe(
                {
                    this.messagesLiveData.value!!.add(it)
                    this.messagesLiveData.value = this.messagesLiveData.value
                },
                { error.value = it.toString() }
            )
    }

    fun getMessages(chatId: Long) {
        messagesRepo.getMessagesForChatBeforeTime(chatId)
            .ioMain()
            .subscribe(
                {
                    this.messagesLiveData.value!!.add(it)
                    this.messagesLiveData.value = this.messagesLiveData.value
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
                    this.messagesLiveData.value!!.add(it)
                    this.messagesLiveData.value = this.messagesLiveData.value
                },
                { error.value = it.toString() }
            )
    }

    fun getUser(userId: Long) {
        usersRepo.getUser(userId)
            .ioMain()
            .subscribe(
                { userLiveData.value = it },
                { error.value = it.toString() }
            )
    }

    fun observeUsers(chatId: Long) {
        usersRepo.observeUsersInChat(chatId)
            .ioMain()
            .subscribe(
                { usersLiveData.value = it },
                { error.value = it.toString() }
            )
    }

    fun deleteMessage(message: Message) {
        messagesRepo.deleteMessage(message)
            .ioMain()
            .subscribe(
                {
                    this.messagesLiveData.value!!.add(it)
                    this.messagesLiveData.value = this.messagesLiveData.value
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
                    this.messagesLiveData.value!!.add(it)
                    this.messagesLiveData.value = this.messagesLiveData.value
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

    fun sendImage(ext: String, data: ByteArray) {
        imageRepo.saveImageMessage(ext, data)
            .ioMain()
            .subscribe(
                {
                    val userId = appSession.userId ?: return@subscribe
                    val message = Message(
                        -1,
                        chatId,
                        userId,
                        "media",
                        "{\"url\": \"${it.url}\", \"width\": ${it.width}, \"height\": ${it.height}}",
                        Date().time
                    )

                    messagesRepo.sendMessage(message)
                        .ioMain()
                        .subscribe(
                            {
                                this.messagesLiveData.value!!.add(it)
                                this.messagesLiveData.value = this.messagesLiveData.value
                            },
                            { error.value = it.toString() }
                        )
                },
                { error.value = it.toString() }
            )
    }

    fun clear() {
        image.value = ""
    }

    fun observeChat(chatId: Long) {
        chatsRepo.observeChat(chatId)
            .ioMain()
            .subscribe(
                { chatLiveData.value = it },
                { error.value = it.toString() }
            )
    }
}
