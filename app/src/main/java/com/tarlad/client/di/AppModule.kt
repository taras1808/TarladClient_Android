package com.tarlad.client.di

import androidx.room.Room
import com.tarlad.client.AppDatabase
import com.tarlad.client.AppSession
import com.tarlad.client.helpers.Preferences
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val appModule = module {

    single {
        Retrofit.Builder()
            .baseUrl("http://192.168.1.18:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java, "database-name"
        ).build()
    }

    single { AppSession() }

    single { Preferences(androidContext()) }
}