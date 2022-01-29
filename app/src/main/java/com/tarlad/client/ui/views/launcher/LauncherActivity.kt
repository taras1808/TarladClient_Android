package com.tarlad.client.ui.views.launcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tarlad.client.AppSession
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.helpers.Preferences
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.states.AppStates
import com.tarlad.client.ui.views.auth.AuthActivity
import com.tarlad.client.ui.views.main.MainActivity
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.net.ConnectException
import java.util.*

class LauncherActivity : AppCompatActivity() {

    private val appSession: AppSession by inject()
    private val tokenDao: TokenDao by inject()
    private val preferences: Preferences by inject()
    private val authRepo: AuthRepo by inject()
    private val socket: Socket by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        socket.on(Socket.EVENT_CONNECT) {
            socket.emit("join")
        }
        socket.on("join") {
            socket.emit("join")
        }
        socket.on(Socket.EVENT_CONNECT_ERROR) { array ->
            array.forEach {
                val refreshToken = tokenDao.getToken()

                if (it !is JSONObject) {
                    return@on
                }

                if (it.getString("message") == "authentication" || refreshToken == null) {
                    socket.disconnect()
                    appSession.state.postValue(AppStates.NotAuthenticated)
                } else if (it.getString("message") == "token") {
                    authRepo.loginWithToken(RefreshTokenDTO(refreshToken.value))
                        .doOnSuccess { token ->
                            authRepo.removeToken(refreshToken)
                            authRepo.saveToken(token.refreshToken)
                        }
                        .doOnError { err ->
                            when (err) {
                                !is ConnectException -> authRepo.removeToken(refreshToken)
                            }
                        }
                        .ioMain()
                        .subscribe(
                            { token ->
                                socket.io().on(Manager.EVENT_TRANSPORT) { array ->
                                    val transport: Transport = array[0] as Transport
                                    transport.on(Transport.EVENT_REQUEST_HEADERS) { args ->
                                        val map: TreeMap<String, List<String>> =
                                            args[0] as TreeMap<String, List<String>>
                                        map["Authorization"] = listOf("Bearer ${token.token}")
                                    }
                                }
                                appSession.state.value = AppStates.Authenticated
                                appSession.token = token.token
                                appSession.userId = token.refreshToken.userId
                                socket.disconnect()
                                socket.connect()
                            },
                            { err ->
                                when (err) {
                                    is ConnectException -> {
                                        appSession.userId = refreshToken.userId
                                    }
                                    else -> {
                                        appSession.state.value = AppStates.NotAuthenticated
                                    }
                                }
                            }
                        )
                }
            }
        }
        preferences.token?.let { token ->
            socket.io().on(Manager.EVENT_TRANSPORT) {
                val transport: Transport = it[0] as Transport
                transport.on(Transport.EVENT_REQUEST_HEADERS) { args ->
                    val map: TreeMap<String, List<String>> =
                        args[0] as TreeMap<String, List<String>>
                    map["Authorization"] = listOf("Bearer $token")
                }
            }
            socket.connect()
        }

        val intent = if (appSession.token == null) {
            Intent(this , AuthActivity::class.java)
        } else {
            Intent(this , MainActivity::class.java)
        }
        startActivity(intent)
        finish()
        super.onCreate(savedInstanceState)
    }
}
