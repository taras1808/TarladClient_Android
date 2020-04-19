package com.tarlad.client.di

import com.tarlad.client.AppDatabase
import com.tarlad.client.api.UsersApi
import com.tarlad.client.repos.UsersRepo
import com.tarlad.client.repos.impl.UsersRepoImpl
import com.tarlad.client.ui.viewLayers.addChat.AddChatActivity
import com.tarlad.client.ui.viewLayers.addChat.AddChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module
import retrofit2.Retrofit

val addChatModule = module {
    scope<AddChatActivity> {
        scoped { get<Retrofit>().create(UsersApi::class.java) }
        scoped { get<AppDatabase>().userDao() }
        scoped<UsersRepo> { UsersRepoImpl(get(), get()) }
    }

    viewModel { (scopeId: ScopeID) ->
        AddChatViewModel(
            getScope(scopeId).get()
        )
    }
}