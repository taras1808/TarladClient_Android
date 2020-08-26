package com.tarlad.client.di

import com.tarlad.client.ui.views.main.MainActivity
import com.tarlad.client.ui.views.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module

val mainModule = module {
    scope<MainActivity> {

    }

    viewModel {  (_: ScopeID) ->
        MainViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}