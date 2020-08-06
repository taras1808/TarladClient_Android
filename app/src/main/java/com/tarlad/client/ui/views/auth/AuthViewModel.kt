package com.tarlad.client.ui.views.auth

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.LoginCredentials
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.states.AppStates
import com.tarlad.client.states.AuthState
import com.tarlad.client.states.Register
import io.reactivex.rxjava3.disposables.Disposable
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.Transport
import java.util.*
import kotlin.collections.HashMap


class AuthViewModel(
    application: Application,
    private val socket: Socket,
    val appSession: AppSession,
    private val authRepo: AuthRepo
) : AndroidViewModel(application) {

    val state = MutableLiveData(AuthState.Login)
    val progressVisibility = MutableLiveData(View.GONE)
    val error = MutableLiveData<String>()

    val registerEmail = MutableLiveData(Register.Empty)
    val registerNickname = MutableLiveData(Register.Empty)

    var checkEmailDisposable: Disposable? = null
    var checkNicknameDisposable: Disposable? = null

    fun checkEmail(email: String) {
        checkEmailDisposable = authRepo.checkEmail(email)
            .ioMain()
            .doOnSubscribe { registerEmail.value = Register.Loading }
            .subscribe(
                { registerEmail.value = Register.Ok },
                { registerEmail.value = Register.Error }
            )
    }

    fun isEmailMatchRegex(email: String): Boolean {
        return email.matches(
            Regex("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6}$")
        )
    }

    fun checkNickname(nickname: String) {
        checkNicknameDisposable = authRepo.checkNickname(nickname)
            .ioMain()
            .doOnSubscribe { registerNickname.value = Register.Loading }
            .subscribe(
                { registerNickname.value = Register.Ok },
                { registerNickname.value = Register.Error }
            )
    }

    private fun checkRegister(
        email: String,
        password: String,
        confirmPassword: String,
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

        if (confirmPassword.isEmpty()) {
            error.value = "Empty confirm password field"
            return false
        }

        if (password != confirmPassword) {
            error.value = "Password and confirm password not equal"
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

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        nickname: String,
        name: String,
        surname: String
    ) {
        if (!checkRegister(email, password, confirmPassword, nickname, name, surname)) return
        val user = User(
            -1,
            email,
            password,
            nickname,
            name,
            surname,
            null
        )
        authRepo.register(user)
            .doOnSuccess { authRepo.saveToken(it.refreshToken) }
            .ioMain()
            .doOnSubscribe { progressVisibility.value = View.VISIBLE }
            .doOnTerminate { progressVisibility.value = View.GONE }
            .subscribe(
                {
                    appSession.userId = it.refreshToken.userId
                    appSession.token = it.token
                    appSession.state.value = AppStates.Authenticated
                },
                { error.value = it.toString() }
            )
    }

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
        val loginInfo = LoginCredentials(email, password)
        authRepo.login(loginInfo)
            .doOnSuccess { authRepo.saveToken(it.refreshToken) }
            .ioMain()
            .doOnSubscribe { progressVisibility.value = View.VISIBLE }
            .subscribe(
                { token ->
                    socket.on(Socket.EVENT_CONNECT, Emitter.Listener {
                        socket.emit("join", token.refreshToken.userId)
                    })
                    appSession.userId = token.refreshToken.userId
                    appSession.token = token.token
                    appSession.state.value = AppStates.Authenticated
                    socket.io().on(Manager.EVENT_TRANSPORT, Emitter.Listener {
                        val transport: Transport = it[0] as Transport
                        transport.on(Transport.EVENT_REQUEST_HEADERS, Emitter.Listener() { args ->
                            val map: TreeMap<String, List<String>> = args[0] as TreeMap<String, List<String>>
                            map["Authorization"] = listOf("Bearer ${token.token}")
                        })
                    })
                    socket.connect()
                },
                {
                    it.printStackTrace()
                    error.value = it.toString()
                    progressVisibility.value = View.GONE
                }
            )
    }
}