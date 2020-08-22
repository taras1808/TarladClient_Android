package com.tarlad.client.di

import com.tarlad.client.ui.views.chat.ChatActivity
import com.tarlad.client.ui.views.chat.ChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module

val chatModule = module {
    scope<ChatActivity> {
    }

    viewModel { (_: ScopeID) ->
        ChatViewModel(
            get(),
            get(),
            get()
        )
    }
}
