package com.tarlad.client.ui.viewLayers.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityAuthBinding
import com.tarlad.client.states.AppStates
import com.tarlad.client.states.AuthState
import com.tarlad.client.ui.viewLayers.auth.fragments.LoginFragment
import com.tarlad.client.ui.viewLayers.auth.fragments.RegisterFragment
import com.tarlad.client.ui.viewLayers.main.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_auth.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AuthActivity : AppCompatActivity() {

    private val vm: AuthViewModel by viewModel { parametersOf(lifecycleScope.id) }
    private val loginFragment by lifecycleScope.inject<LoginFragment>()
    private val registerFragment by lifecycleScope.inject<RegisterFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityAuthBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_auth)
        binding.vm = vm
        binding.lifecycleOwner = this

        observeAppState()

        vm.tryLoginWithToken()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}) {
                observeFragmentState()
                observeError()
            }
    }

    private fun observeAppState() {
        vm.appSession.state.observe(this, Observer {
            if (it == AppStates.Authenticated) {
               startActivity(Intent(this, MainActivity::class.java))
            }
        })
    }

    private fun observeError() {
        vm.error.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                val snack = Snackbar.make(auth_layout, it, Snackbar.LENGTH_LONG)
                snack.setBackgroundTint(Color.RED)
                snack.show()
            }
        })
        vm.error.value = null
    }

    private fun observeFragmentState() {
        vm.state.observe(this, Observer {
            when (it) {
                AuthState.Login -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, loginFragment).commit()
                AuthState.Register -> supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, registerFragment).commit()
                else -> {
                }
            }
        })
    }
}
