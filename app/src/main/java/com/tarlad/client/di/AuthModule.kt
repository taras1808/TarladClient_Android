package com.tarlad.client.di

import com.tarlad.client.api.AuthApi
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.repos.impl.AuthRepoImpl
import com.tarlad.client.ui.views.auth.AuthActivity
import com.tarlad.client.ui.views.auth.AuthViewModel
import com.tarlad.client.ui.views.auth.fragments.LoginFragment
import com.tarlad.client.ui.views.auth.fragments.RegisterFragment
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module
import retrofit2.Retrofit

val authModule = module {
    scope<AuthActivity> {
        scoped { LoginFragment() }
        scoped { RegisterFragment() }
    }

    viewModel {(scopeId: ScopeID) ->
        AuthViewModel(
            androidApplication(),
            get(),
            get()
        )
    }
}