package com.tarlad.client.ui.views.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityAuthBinding
import com.tarlad.client.states.AppStates
import com.tarlad.client.states.AuthState
import com.tarlad.client.ui.views.auth.fragments.LoginFragment
import com.tarlad.client.ui.views.auth.fragments.RegisterFragment
import com.tarlad.client.ui.views.main.MainActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthActivity : AppCompatActivity() {

    private val vm: AuthViewModel by viewModel()
    private val loginFragment = LoginFragment()
    private val registerFragment = RegisterFragment()
    lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeError()
        observeAppState()
        observeProgressVisibility()
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

    private fun observeProgressVisibility() {
        vm.progressVisibility.observe(this, Observer {
            binding.linearLayout.visibility = it
        })
    }

    private fun observeFragmentState() {
        vm.state.observe(this, Observer {
            when (it) {
                AuthState.Login ->
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.activity_back_in, R.anim.activity_back_out)
                        .replace(R.id.fragment_container, loginFragment).commit()
                AuthState.Register ->
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.activity_back_in, R.anim.activity_back_out)
                        .replace(R.id.fragment_container, registerFragment).commit()
                else -> {}
            }
        })
    }

    private fun observeError() {
        vm.error.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                val snack = Snackbar.make(binding.authContainer, it, Snackbar.LENGTH_LONG)
                snack.setBackgroundTint(
                    ContextCompat.getColor(applicationContext, R.color.colorError)
                )
                snack.show()
            }
        })
        vm.error.value = null
    }
}
