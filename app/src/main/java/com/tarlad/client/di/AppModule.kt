package com.tarlad.client.di

import androidx.room.Room
import com.tarlad.client.AppDatabase
import com.tarlad.client.AppSession
import com.tarlad.client.api.AuthApi
import com.tarlad.client.api.ChatsApi
import com.tarlad.client.api.MessageApi
import com.tarlad.client.api.UsersApi
import com.tarlad.client.helpers.Preferences
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.MessagesRepo
import com.tarlad.client.repos.UsersRepo
import com.tarlad.client.repos.impl.AuthRepoImpl
import com.tarlad.client.repos.impl.ChatsRepoImpl
import com.tarlad.client.repos.impl.MessagesRepoImpl
import com.tarlad.client.repos.impl.UsersRepoImpl
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val appModule = module {

    single {
        Retrofit.Builder()
            .baseUrl("http://192.168.0.108:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java, "tarlad"
        ).build()
    }

    single { AppSession() }

    single { get<AppDatabase>().tokenDao() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().chatDao() }
    single { get<AppDatabase>().messagesDao() }

    single { Preferences(androidContext()) }

    single<AuthRepo> { AuthRepoImpl(get(), get()) }
    single<UsersRepo> { UsersRepoImpl(get(), get()) }
    single<ChatsRepo> { ChatsRepoImpl(get()) }
    single<MessagesRepo> { MessagesRepoImpl(get()) }

    single { get<Retrofit>().create(AuthApi::class.java) }
    single { get<Retrofit>().create(ChatsApi::class.java) }
    single { get<Retrofit>().create(UsersApi::class.java) }
    single { get<Retrofit>().create(MessageApi::class.java) }
}