package com.tarlad.client.ui.views.chat.participants

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.tarlad.client.App
import com.tarlad.client.AppSession
import com.tarlad.client.R
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.UsersRepo
import io.reactivex.rxjava3.disposables.Disposable
import java.util.ArrayList

class ChatAddParticipantsViewModel(
    application: Application,
    private val usersRepo: UsersRepo,
    private val chatsRepo: ChatsRepo
): AndroidViewModel(application) {

    val search = MutableLiveData<String>()

    val complete = MutableLiveData<Boolean>(false)

    val error = MutableLiveData<String>()
    val users = MutableLiveData<List<User>>()
    val success = MutableLiveData<Boolean>(false)

    var searchUsersDisposable: Disposable? = null

    var page = 0

    fun search(chatId: Long) {
        searchUsersDisposable = usersRepo.searchUsersForChat(search.value ?: "", chatId, page++)
            .ioMain()
            .subscribe(
                { users.value = it },
                { error.value = it.toString() },
                { complete.value = true }
            )
    }

    fun addParticipants(chatId: Long, users: ArrayList<Long>) {
        if (users.size == 0) {
            error.value = getApplication<App>().getString(R.string.choose_users)
            return
        }
        chatsRepo.addParticipants(chatId, users)
            .ioMain()
            .subscribe(
                { success.value = true },
                { error.value = it.toString() }
            )
    }
}
