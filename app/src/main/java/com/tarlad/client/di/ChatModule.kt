package com.tarlad.client.di

import com.tarlad.client.AppDatabase
import com.tarlad.client.api.UsersApi
import com.tarlad.client.repos.ChatsRepo
import com.tarlad.client.repos.MainRepo
import com.tarlad.client.repos.UsersRepo
import com.tarlad.client.repos.impl.ChatsRepoImpl
import com.tarlad.client.repos.impl.MainRepoImpl
import com.tarlad.client.repos.impl.UsersRepoImpl
import com.tarlad.client.ui.views.addChat.AddChatActivity
import com.tarlad.client.ui.views.addChat.AddChatViewModel
import com.tarlad.client.ui.views.chat.ChatActivity
import com.tarlad.client.ui.views.chat.ChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module
import retrofit2.Retrofit

val chatModule = module {
    scope<ChatActivity> {
    }

    viewModel { (scopeId: ScopeID) ->
        ChatViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}