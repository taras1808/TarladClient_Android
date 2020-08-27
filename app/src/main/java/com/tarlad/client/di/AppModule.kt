package com.tarlad.client.di

import androidx.room.Room
import com.tarlad.client.AppDatabase
import com.tarlad.client.AppSession
import com.tarlad.client.api.AuthApi
import com.tarlad.client.api.ChatsApi
import com.tarlad.client.api.UsersApi
import com.tarlad.client.helpers.Preferences
import com.tarlad.client.repos.*
import com.tarlad.client.repos.impl.*
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.socket.client.IO
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


val appModule = module {

    single {
        IO.socket("http://192.168.88.254:3000/")
    }

    single {
        Retrofit.Builder()
            .baseUrl("http://192.168.88.254:3000/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java, "tarlad"
        ).build()
    }

    single { AppSession(get()) }

    single { get<AppDatabase>().tokenDao() }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().chatDao() }
    single { get<AppDatabase>().chatListDao() }
    single { get<AppDatabase>().messagesDao() }

    single { Preferences(androidContext()) }

    single<AuthRepo> { AuthRepoImpl(get(), get()) }
    single<UsersRepo> { UsersRepoImpl(get(), get(), get()) }
    single<ChatsRepo> { ChatsRepoImpl(get(), get(), get(), get()) }
    single<MessagesRepo> { MessagesRepoImpl(get(), get()) }
    single<MainRepo> { MainRepoImpl(get(), get(), get(), get(), get()) }

    single { get<Retrofit>().create(AuthApi::class.java) }
    single { get<Retrofit>().create(ChatsApi::class.java) }
    single { get<Retrofit>().create(UsersApi::class.java) }
}
