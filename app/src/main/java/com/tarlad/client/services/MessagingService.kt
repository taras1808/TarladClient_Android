package com.tarlad.client.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.tarlad.client.AppSession
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.helpers.Preferences
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.states.AppStates
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import org.koin.android.ext.android.inject
import java.net.ConnectException
import java.util.*


@Suppress("UNCHECKED_CAST")
class MessagingService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val socket: Socket by inject()
    private val tokenDao: TokenDao by inject()
    private val preferences: Preferences by inject()
    private val appSession: AppSession by inject()
    private val authRepo: AuthRepo by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        socket.on(Socket.EVENT_CONNECT) {
            socket.emit("join")
        }

        socket.on("join") {
            socket.emit("join")
        }

        socket.on("error") { array ->
            array.forEach {
                val refreshToken = tokenDao.getToken()
                if (it == "authentication" || refreshToken == null) {
                    appSession.state.postValue(AppStates.NotAuthenticated)
                    socket.disconnect()
                } else if (it == "token") {
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

//        socket.on("message", Emitter.Listener {
//            val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
//            messageDao.insert(message)
//
//            val builder = NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
//                .setSmallIcon(R.drawable.ic_notification_overlay)
//                .setContentTitle("My notification")
//                .setContentText(message.data)
//                .setStyle(NotificationCompat.BigTextStyle()
//                    .bigText(message.data))
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//
//            with(NotificationManagerCompat.from(this)) {
//                notify(1, builder.build())
//            }
//
//        })

        preferences.token?.let { token ->
            socket.io().on(Manager.EVENT_TRANSPORT) {
                val transport: Transport = it[0] as Transport
                transport.on(Transport.EVENT_REQUEST_HEADERS) { args ->
                    val map: TreeMap<String, List<String>> =
                        args[0] as TreeMap<String, List<String>>
                    map["Authorization"] = listOf("Bearer $token")
                }
            }
        }


        socket.connect()

//        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel(
//                "YOUR_CHANNEL_ID",
//                "YOUR_CHANNEL_NAME",
//                NotificationManager.IMPORTANCE_DEFAULT
//            )
//        } else {
//            null
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            channel?.description = "YOUR_NOTIFICATION_CHANNEL_DESCRIPTION"
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            channel?.let {
//                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
//                    it
//                )
//            }
//        }

        return super.onStartCommand(intent, flags, startId)
    }
}
