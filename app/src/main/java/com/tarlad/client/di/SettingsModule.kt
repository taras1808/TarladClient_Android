package com.tarlad.client.di

import com.tarlad.client.ui.views.main.MainActivity
import com.tarlad.client.ui.views.main.MainViewModel
import com.tarlad.client.ui.views.settings.SettingsActivity
import com.tarlad.client.ui.views.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.scope.ScopeID
import org.koin.dsl.module

val settingsModule = module {
    scope<SettingsActivity> {

    }

    viewModel {  (_: ScopeID) ->
        SettingsViewModel(
            get(),
            get(),
            get()
        )
    }
}