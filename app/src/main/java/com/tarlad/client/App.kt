package com.tarlad.client

import android.app.Application
import android.content.Intent
import com.tarlad.client.di.addChatModule
import com.tarlad.client.di.appModule
import com.tarlad.client.di.authModule
import com.tarlad.client.di.mainModule
import com.tarlad.client.services.MessagingService
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                appModule,
                authModule,
                mainModule,
                addChatModule
            )
        }

        startService(Intent(this, MessagingService::class.java))
    }
}