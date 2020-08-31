package com.tarlad.client.di

import com.tarlad.client.ui.views.chat.details.ChatDetailsActivity
import com.tarlad.client.ui.views.chat.details.ChatDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module

val chatDetailsModule = module {
    scope<ChatDetailsActivity> {
    }

    viewModel { (_: ScopeID) ->
        ChatDetailsViewModel(
            get(),
            get(),
            get()
        )
    }
}
