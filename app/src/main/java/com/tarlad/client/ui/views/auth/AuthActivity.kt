package com.tarlad.client.ui.views.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityAuthBinding
import com.tarlad.client.states.AppStates
import com.tarlad.client.states.AuthState
import com.tarlad.client.ui.views.auth.fragments.LoginFragment
import com.tarlad.client.ui.views.auth.fragments.RegisterFragment
import com.tarlad.client.ui.views.main.MainActivity
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

        observeError()
        observeAppState()
        observeFragmentState()
    }

    private fun observeAppState() {
        vm.appSession.state.observe(this, Observer {
            if (it == AppStates.Authenticated) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun observeFragmentState() {
        vm.state.observe(this, Observer {
            when (it) {
                AuthState.Login ->
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                        .replace(R.id.fragment_container, loginFragment).commit()
                AuthState.Register ->
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
                        .replace(R.id.fragment_container, registerFragment).commit()
                else -> {}
            }
        })
    }

    private fun observeError() {
        vm.error.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                val snack = Snackbar.make(auth_container, it, Snackbar.LENGTH_LONG)
                snack.setBackgroundTint(
                    ContextCompat.getColor(applicationContext, R.color.colorError)
                )
                snack.show()
            }
        })
        vm.error.value = null
    }
}
