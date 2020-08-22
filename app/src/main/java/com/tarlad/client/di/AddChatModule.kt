package com.tarlad.client.di

import com.tarlad.client.ui.views.chat.create.ChatCreateActivity
import com.tarlad.client.ui.views.chat.create.ChatCreateViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module

val addChatModule = module {
    scope<ChatCreateActivity> {

    }

    viewModel { (_: ScopeID) ->
        ChatCreateViewModel(
            get(),
            get(),
            get()
        )
    }
}
