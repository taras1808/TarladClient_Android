package com.tarlad.client.services

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.tarlad.client.AppSession
import com.tarlad.client.R
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.helpers.Preferences
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.dto.RefreshTokenDTO
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.states.AppStates
import com.tarlad.client.ui.views.chat.ChatActivity
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.Transport
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.net.ConnectException
import java.util.*


@Suppress("UNCHECKED_CAST")
class MessagingService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val socket: Socket by inject()

    @SuppressLint("WrongConstant", "UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        socket.on("messages", Emitter.Listener {
            val message: Message = Gson().fromJson(it[0].toString(), Message::class.java)
            val pending = PendingIntent.getActivity(this,0, Intent(this, ChatActivity::class.java).apply {
                putExtra("ID", message.chatId)
                putExtra("TITLE", "Hello")
            }, Intent.FLAG_ACTIVITY_NEW_TASK)
            val builder = NotificationCompat.Builder(this, "YOUR_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("My notification")
                .setContentText(message.data)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(message.data))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.button, "Click", pending)
            with(NotificationManagerCompat.from(this)) {
                notify(1, builder.build())
            }
        })

        val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                "YOUR_CHANNEL_ID",
                "YOUR_CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setShowBadge(true)

            }
        } else {
            null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel?.description = "YOUR_NOTIFICATION_CHANNEL_DESCRIPTION"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel?.let {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    it
                )
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
