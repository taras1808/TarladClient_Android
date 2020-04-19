package com.tarlad.client.ui.viewLayers.auth

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.Preferences
import com.tarlad.client.models.LoginInfo
import com.tarlad.client.models.Token
import com.tarlad.client.models.User
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.states.AppStates
import com.tarlad.client.states.AuthState
import com.tarlad.client.states.RegisterEmail
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    application: Application,
    val repo: AuthRepo,
    val appSession: AppSession,
    val prefs: Preferences
) :
    AndroidViewModel(application) {
    val state = MutableLiveData(AuthState.Login)
    val progressVisibility = MutableLiveData(View.GONE)
    val error = MutableLiveData<String>()

    /**
     * Register
     */
    val registerEmail = MutableLiveData<RegisterEmail>(RegisterEmail.Empty)

    fun checkEmail(email: String) {
        GlobalScope.launch(Dispatchers.Main) {
            registerEmail.value = RegisterEmail.Loading
            val res = withContext(Dispatchers.IO) { repo.checkEmail(email) }
            if (res)
                registerEmail.value = RegisterEmail.Error
            else
                registerEmail.value = RegisterEmail.Ok
        }
    }

    fun isEmailMatchRegex(email: String): Boolean {
        return email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))
    }

    private fun checkRegister(
        email: String,
        password: String,
        name: String,
        surname: String
    ): Boolean {
        if (email.isEmpty()) {
            error.value = "Empty email field"
            return false
        } else if (!isEmailMatchRegex(email)) {
            error.value = "Bad email address"
            return false
        } else if (password.isEmpty()) {
            error.value = "Empty password field"
            return false
        } else if (name.isEmpty()) {
            error.value = "Empty name field"
            return false
        } else if (surname.isEmpty()) {
            error.value = "Empty surname field"
            return false
        } else
            return true
    }

    fun register(email: String, password: String, name: String, surname: String) {
        if (checkRegister(email, password, name, surname)) {
            progressVisibility.value = View.VISIBLE
            repo.register(User(email = email, password = password, name = name, surname = surname))
                .subscribeOn(Schedulers.io())
                .doOnComplete { progressVisibility.postValue(View.GONE) }
                .subscribe({
                    authenticate(it)
                },
                    {
                        error.postValue(it.toString())
                    })
        }
    }


    /**
     * Login
     */

    fun tryLoginWithToken(): Observable<Unit> {
        return Observable.create { emitter ->
            val token = prefs.token
            val id = prefs.idUser
            if (token.isEmpty() || id == -1) {
                emitter.onError(null)
            } else {
                repo.loginWithToken(Token(id, token))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        {
                            authenticate(it)
                        },
                        {
                            it.printStackTrace()
                            emitter.onError(null)
                        }
                    )
            }
        }
    }

    private fun checkLogin(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            error.value = "Empty email field"
            return false
        } else if (!isEmailMatchRegex(email)) {
            error.value = "Bad email address"
            return false
        } else if (password.isEmpty()) {
            error.value = "Empty password field"
            return false
        }
        return true
    }

    fun login(email: String, password: String) {
        if (checkLogin(email, password)) {
            progressVisibility.value = View.VISIBLE
            repo.login(LoginInfo(email, password))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        authenticate(it)
                        progressVisibility.value = View.GONE
                    },
                    {
                        error.postValue(it.toString())
                        progressVisibility.value = View.GONE
                    })
        }
    }


    /**
     * Private methods
     */
    private fun authenticate(token: Token?) {
        if (token != null) {
            repo.saveToken(token)
            appSession.token = token
            appSession.state.value = AppStates.Authenticated
        } else {
            error.postValue("Bad token")
        }
    }
}