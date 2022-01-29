package com.tarlad.client.ui.views.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.User
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.repos.UsersRepo
import com.tarlad.client.states.Register
import io.reactivex.rxjava3.disposables.Disposable

class SettingsViewModel(
    private val appSession: AppSession,
    private val authRepo: AuthRepo,
    private val usersRepo: UsersRepo
): ViewModel() {

    val error = MutableLiveData<String>()

    val name = MutableLiveData<String>()
    val surname = MutableLiveData<String>()
    val nickname = MutableLiveData<String>()

    var oldName: String = ""
    var oldSurname: String = ""
    var oldNickname: String = ""

    val nicknameProgress = MutableLiveData(false)
    val nicknameStatus = MutableLiveData(false)

    val nicknameState = MutableLiveData(Register.Empty)

    var checkNicknameDisposable: Disposable? = null

    fun getUser() {
        usersRepo.getUser(appSession.userId!!)
            .ioMain()
            .subscribe(
                {
                    oldName = it.name
                    oldSurname = it.surname
                    oldNickname = it.nickname
                    name.value = it.name
                    surname.value = it.surname
                    nickname.value = it.nickname
                },
                { error.value = it.toString()}
            )
    }

    fun checkNickname(nickname: String) {
        if (nickname == oldNickname) {
            nicknameState.value = Register.Empty
            return
        }
        checkNicknameDisposable = authRepo.checkNickname(nickname)
            .ioMain()
            .doOnSubscribe { nicknameState.value = Register.Loading }
            .subscribe(
                { nicknameState.value = Register.Ok },
                { nicknameState.value = Register.Error }
            )
    }

    fun updateUser() {
        usersRepo.updateUser(nickname.value!!, name.value!!, surname.value!!)
            .ioMain()
            .subscribe(
                {
                    oldName = it.name
                    oldSurname = it.surname
                    oldNickname = it.nickname
                    nicknameState.value = Register.Empty
                },
                { error.value = it.toString() }
            )
    }
}
