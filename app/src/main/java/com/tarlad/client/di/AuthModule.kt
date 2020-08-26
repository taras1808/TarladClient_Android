package com.tarlad.client.di

import com.tarlad.client.ui.views.auth.AuthActivity
import com.tarlad.client.ui.views.auth.AuthViewModel
import com.tarlad.client.ui.views.auth.fragments.LoginFragment
import com.tarlad.client.ui.views.auth.fragments.RegisterFragment
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module

val authModule = module {
    scope<AuthActivity> {
    }

    viewModel {(_: ScopeID) ->
        AuthViewModel(
            androidApplication(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
