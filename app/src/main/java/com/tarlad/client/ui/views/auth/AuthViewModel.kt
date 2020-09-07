package com.tarlad.client.ui.views.auth

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.tarlad.client.App
import com.tarlad.client.AppSession
import com.tarlad.client.R
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.LoginCredentials
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.repos.MainRepo
import com.tarlad.client.states.AppStates
import com.tarlad.client.states.AuthState
import com.tarlad.client.states.Register
import io.reactivex.rxjava3.disposables.Disposable
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import java.util.*


@Suppress("UNCHECKED_CAST")
class AuthViewModel(
    application: Application,
    private val socket: Socket,
    val appSession: AppSession,
    private val authRepo: AuthRepo,
    private val mainRepo: MainRepo
) : AndroidViewModel(application) {

    val loginEmail = MutableLiveData<String>()
    val loginPassword = MutableLiveData<String>()

    val registerEmail = MutableLiveData<String>()
    val registerPassword = MutableLiveData<String>()
    val registerConfirmPassword = MutableLiveData<String>()
    val registerNickname = MutableLiveData<String>()
    val registerName = MutableLiveData<String>()
    val registerSurname = MutableLiveData<String>()

    val state = MutableLiveData(AuthState.Login)
    val progressVisibility = MutableLiveData(View.GONE)
    val error = MutableLiveData<String>()

    val registerEmailState = MutableLiveData(Register.Empty)
    val registerNicknameState = MutableLiveData(Register.Empty)

    val emailProgress = MutableLiveData(false)
    val emailStatus = MutableLiveData(false)

    val nicknameProgress = MutableLiveData(false)
    val nicknameStatus = MutableLiveData(false)

    var checkEmailDisposable: Disposable? = null
    var checkNicknameDisposable: Disposable? = null

    fun checkEmail(email: String) {
        checkEmailDisposable = authRepo.checkEmail(email)
            .ioMain()
            .doOnSubscribe { registerEmailState.value = Register.Loading }
            .subscribe(
                { registerEmailState.value = Register.Ok },
                { registerEmailState.value = Register.Error }
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
            .doOnSubscribe { registerNicknameState.value = Register.Loading }
            .subscribe(
                { registerNicknameState.value = Register.Ok },
                {
                    println(it.toString())
                    registerNicknameState.value = Register.Error
                }
            )
    }

    private fun checkRegister(
        email: String?,
        password: String?,
        confirmPassword: String?,
        nickname: String?,
        name: String?,
        surname: String?
    ): Boolean {
        if (email.isNullOrEmpty()) {
            error.value = getApplication<App>().getString(R.string.empty_email_field)
            return false
        }

        if (!isEmailMatchRegex(email)) {
            error.value = getApplication<App>().getString(R.string.bad_email_address)
            return false
        }

        if (password.isNullOrEmpty()) {
            error.value = getApplication<App>().getString(R.string.empty_password_field)
            return false
        }

        if (confirmPassword.isNullOrEmpty()) {
            error.value = getApplication<App>().getString(R.string.empty_confirm_password_field)
            return false
        }

        if (password != confirmPassword) {
            error.value = getApplication<App>().getString(R.string.password_and_confirm_password_are_not_equal)
            return false
        }

        if (nickname.isNullOrEmpty()) {
            error.value = getApplication<App>().getString(R.string.empty_nickname_field)
            return false
        }

        if (name.isNullOrEmpty()) {
            error.value = getApplication<App>().getString(R.string.empty_name_field)
            return false
        }

        if (surname.isNullOrEmpty()) {
            error.value = getApplication<App>().getString(R.string.empty_surname_field)
            return false
        }
        return true
    }

    fun register() {
        val email = registerEmail.value
        val password = registerPassword.value
        val confirmPassword = registerConfirmPassword.value
        val nickname = registerNickname.value
        val name = registerName.value
        val surname = registerSurname.value
        if (!checkRegister(email, password, confirmPassword, nickname, name, surname)) return
        val user = User(
            -1,
            email,
            password,
            nickname!!,
            name!!,
            surname!!,
            null
        )
        authRepo.register(user)
            .doOnSuccess { mainRepo.truncate() }
            .doOnSuccess { authRepo.saveToken(it.refreshToken) }
            .ioMain()
            .doOnSubscribe { progressVisibility.value = View.VISIBLE }
            .doOnTerminate {
                progressVisibility.value = View.GONE
            }
            .subscribe(
                {
                    appSession.userId = it.refreshToken.userId
                    appSession.token = it.token
                    appSession.state.value = AppStates.Authenticated
                },
                { error.value = it.toString() }
            )
    }

    private fun checkLogin(email: String?, password: String?): Boolean {
        if (email.isNullOrEmpty()) {
            error.value = getApplication<App>().getString(R.string.empty_email_field)
            return false
        }

        if (!isEmailMatchRegex(email)) {
            error.value = getApplication<App>().getString(R.string.bad_email_address)
            return false
        }

        if (password.isNullOrEmpty()) {
            error.value = getApplication<App>().getString(R.string.empty_password_field)
            return false
        }
        return true
    }

    fun login() {
        val email = loginEmail.value
        val password = loginPassword.value
        if (!checkLogin(email, password)) return
        val loginInfo = LoginCredentials(email!!, password!!)
        authRepo.login(loginInfo)
            .doOnSuccess { mainRepo.truncate() }
            .doOnSuccess { authRepo.saveToken(it.refreshToken) }
            .ioMain()
            .doOnSubscribe { progressVisibility.value = View.VISIBLE }
            .subscribe(
                { token ->
                    appSession.userId = token.refreshToken.userId
                    appSession.token = token.token
                    appSession.state.value = AppStates.Authenticated
                    socket.io().on(Manager.EVENT_TRANSPORT) {
                        val transport: Transport = it[0] as Transport
                        transport.on(Transport.EVENT_REQUEST_HEADERS) { args ->
                            val map: TreeMap<String, List<String>> =
                                args[0] as TreeMap<String, List<String>>
                            map["Authorization"] = listOf("Bearer ${token.token}")
                        }
                    }
                    socket.connect()
                },
                {
                    it.printStackTrace()
                    error.value = it.toString()
                    progressVisibility.value = View.GONE
                }
            )
    }

    fun switchToRegistration() {
        state.value = AuthState.Register
    }

    fun switchToLogin() {
        state.value = AuthState.Login
    }
}
