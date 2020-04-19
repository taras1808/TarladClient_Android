package com.tarlad.client.di

import com.tarlad.client.api.AuthApi
import com.tarlad.client.repos.AuthRepo
import com.tarlad.client.repos.impl.AuthRepoImpl
import com.tarlad.client.ui.viewLayers.auth.AuthActivity
import com.tarlad.client.ui.viewLayers.auth.AuthViewModel
import com.tarlad.client.ui.viewLayers.auth.fragments.LoginFragment
import com.tarlad.client.ui.viewLayers.auth.fragments.RegisterFragment
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module
import retrofit2.Retrofit

val authModule = module {
    scope<AuthActivity> {
        scoped { LoginFragment() }
        scoped { RegisterFragment() }
        scoped { get<Retrofit>().create(AuthApi::class.java) }
        scoped<AuthRepo> { AuthRepoImpl(get(), get(), get()) }


    }

    viewModel { (scopeId: ScopeID) ->
        AuthViewModel(
            androidApplication(),
            getScope(scopeId).get(),
            get(),
            get()
        )
    }
}