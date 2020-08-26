package com.tarlad.client.di

import com.tarlad.client.ui.views.chat.participants.ChatAddParticipantsActivity
import com.tarlad.client.ui.views.chat.participants.ChatAddParticipantsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module

val chatAddParticipantsModule = module {
    scope<ChatAddParticipantsActivity> {
    }

    viewModel { (_: ScopeID) ->
        ChatAddParticipantsViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
}
