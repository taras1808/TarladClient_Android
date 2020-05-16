package com.tarlad.client.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.concurrent.thread

class MessagingService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        val ds = DatagramSocket()
//
//        val alive = byteArrayOf(1)
//        val aliveDp = DatagramPacket(alive, alive.size, InetAddress.getByName("192.168.1.18"), 7894)
//        thread {
//            while (true) {
//                ds.send(aliveDp)
//                println("sent")
//
//                Thread.sleep(3000)
//            }
//        }
//
//
//        thread {
//            val buf = ByteArray(16)
//            val dp = DatagramPacket(buf, buf.size)
//
//            while (true) {
//                ds.receive(dp)
//
//                println(String(buf))
//            }
//        }
        return super.onStartCommand(intent, flags, startId)
    }
}