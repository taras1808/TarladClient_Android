package com.tarlad.client.ui.views.auth.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.tarlad.client.R
import com.tarlad.client.states.AuthState
import com.tarlad.client.ui.views.auth.AuthViewModel
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoginFragment: Fragment(R.layout.fragment_login) {
    private val vm: AuthViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_btn.setOnClickListener {
            vm.login(edit_email.text.toString(), edit_password.text.toString())
        }

        register_btn.setOnClickListener {
            vm.state.value = AuthState.Register
        }
    }
}