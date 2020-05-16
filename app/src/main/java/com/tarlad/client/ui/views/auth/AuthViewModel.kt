package com.tarlad.client.ui.views.auth

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.OnComplete
import com.tarlad.client.helpers.OnError
import com.tarlad.client.models.LoginInfo
import com.tarlad.client.models.Token
import com.tarlad.client.models.User
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.states.AppStates
import com.tarlad.client.states.AuthState
import com.tarlad.client.states.Register
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import java.lang.Exception

class AuthViewModel(
    application: Application,
    private val authRepo: AuthRepo,
    val appSession: AppSession
) : AndroidViewModel(application) {

    val state = MutableLiveData(AuthState.Login)
    val progressVisibility = MutableLiveData(View.GONE)
    val error = MutableLiveData<String>()

    /**
     * Register
     */
    val registerEmail = MutableLiveData<Register>(Register.Empty)
    val registerNickname = MutableLiveData<Register>(Register.Empty)

    var checkEmailJob: Job? = null
    var checkNicknameJob: Job? = null

    fun checkEmail(email: String) {
        checkEmailJob = CoroutineScope(Dispatchers.Main).launch {
            registerEmail.value = Register.Loading
            when (withContext(Dispatchers.IO) { authRepo.checkEmail(email) }){
                is OnComplete -> registerEmail.value = Register.Ok
                is OnError -> {
                    //TODO check server error
                    registerEmail.value = Register.Error
                }
            }
        }
    }

    fun isEmailMatchRegex(email: String): Boolean {
        //TODO Regex
        return email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))
    }

    fun checkNickname(nickname: String) {
        checkNicknameJob = CoroutineScope(Dispatchers.Main).launch {
            registerNickname.value = Register.Loading
            when (withContext(Dispatchers.IO) { authRepo.checkNickname(nickname) }){
                is OnComplete -> registerNickname.value = Register.Ok
                is OnError -> {
                    //TODO check server error
                    registerNickname.value = Register.Error
                }
            }
        }
    }

    private fun checkRegister(
        email: String,
        password: String,
        nickname: String,
        name: String,
        surname: String
    ): Boolean {
        if (email.isEmpty()) {
            error.value = "Empty email field"
            return false
        }

        if (!isEmailMatchRegex(email)) {
            error.value = "Bad email address"
            return false
        }

        if (password.isEmpty()) {
            error.value = "Empty password field"
            return false
        }

        if (nickname.isEmpty()) {
            error.value = "Empty nickname field"
            return false
        }

        if (name.isEmpty()) {
            error.value = "Empty name field"
            return false
        }

        if (surname.isEmpty()) {
            error.value = "Empty surname field"
            return false
        }
        return true
    }

    fun register(email: String, password: String, nickname: String, name: String, surname: String) {
        if (!checkRegister(email, password, nickname, name, surname)) return
        progressVisibility.value = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            when (val res = withContext(Dispatchers.IO) { authRepo.register(User(null, email, password, nickname, name, surname, null)) }){
                is OnComplete -> saveToken(res.data)
                is OnError -> error.value = res.t.toString()
            }
            progressVisibility.postValue(View.GONE)
        }
    }

    /**
     * Login
     */
    private fun checkLogin(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            error.value = "Empty email field"
            return false
        }

        if (!isEmailMatchRegex(email)) {
            error.value = "Bad email address"
            return false
        }

        if (password.isEmpty()) {
            error.value = "Empty password field"
            return false
        }
        return true
    }

    fun login(email: String, password: String) {
        if (!checkLogin(email, password)) return
        progressVisibility.value = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            when (val res =
                withContext(Dispatchers.IO) { authRepo.login(LoginInfo(email, password)) }) {
                is OnComplete -> saveToken(res.data)
                is OnError -> {
                    error.value = res.t.toString()
                    progressVisibility.value = View.GONE
                }
            }
        }
    }

    /**
     * Private methods
     */
    private fun saveToken(token: Token) {
        CoroutineScope(Dispatchers.IO).launch {
            authRepo.saveToken(token)
        }
        appSession.token = token
        appSession.state.value = AppStates.Authenticated
    }
}