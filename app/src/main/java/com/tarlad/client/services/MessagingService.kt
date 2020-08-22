package com.tarlad.client.services

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.tarlad.client.AppSession
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.helpers.Preferences
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.states.AppStates
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.Transport
import org.koin.android.ext.android.inject
import org.koin.core.logger.MESSAGE
import java.net.ConnectException
import java.util.*


class MessagingService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val socket: Socket by inject()
    private val messageDao: MessageDao by inject()
    private val tokenDao: TokenDao by inject()
    private val preferences: Preferences by inject()
    private val appSession: AppSession by inject()
    private val authRepo: AuthRepo by inject()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        socket.on(Socket.EVENT_CONNECT, Emitter.Listener {
            socket.emit("join")
        })

        socket.on("join", Emitter.Listener {
            socket.emit("join")
        })

//        socket.on("del") {i ->
//            messageDao.getById(i[0].toString())?.let {
//                println(it)
//                messageDao.delete(it)
//                println(messageDao.getById(it.id.toString()))
//            }
//        }

        socket.on("error") {
            it.forEach {
                val refreshToken = tokenDao.getToken()
                if (it == "authentication" || refreshToken == null) {
                    appSession.state.postValue(AppStates.NotAuthenticated)
                    socket.disconnect()
                } else if (it == "token") {
                    authRepo.loginWithToken(RefreshTokenDTO(refreshToken.value))
                        .doOnSuccess {
                            authRepo.removeToken(refreshToken)
                            authRepo.saveToken(it.refreshToken)
                        }
                        .doOnError {
                            when (it) {
                                !is ConnectException -> authRepo.removeToken(refreshToken)
                            }
                        }
                        .ioMain()
                        .subscribe(
                            { token ->
                                socket.io().on(Manager.EVENT_TRANSPORT, Emitter.Listener {
                                    val transport: Transport = it[0] as Transport
                                    transport.on(Transport.EVENT_REQUEST_HEADERS, Emitter.Listener() { args ->
                                        val map: TreeMap<String, List<String>> = args[0] as TreeMap<String, List<String>>
                                        map["Authorization"] = listOf("Bearer ${token.token}")
                                    })
                                })
                                appSession.state.value = AppStates.Authenticated
                                appSession.token = token.token
                                appSession.userId = token.refreshToken.userId
                                socket.disconnect()
                                socket.connect()
                            },
                            {
                                when (it) {
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
////            val builder = NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
////                .setSmallIcon(R.drawable.ic_notification_overlay)
////                .setContentTitle("My notification")
////                .setContentText(message.data)
////                .setStyle(NotificationCompat.BigTextStyle()
////                    .bigText(message.data))
////                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
////
////            with(NotificationManagerCompat.from(this)) {
////                notify(1, builder.build())
////            }
//
//        })

        preferences.token?.let { token ->
            socket.io().on(Manager.EVENT_TRANSPORT, Emitter.Listener {
                val transport: Transport = it[0] as Transport
                transport.on(Transport.EVENT_REQUEST_HEADERS, Emitter.Listener() { args ->
                    val map: TreeMap<String, List<String>> = args[0] as TreeMap<String, List<String>>
                    map["Authorization"] = listOf("Bearer $token")
                })
            })
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