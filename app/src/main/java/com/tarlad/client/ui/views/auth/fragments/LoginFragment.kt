package com.tarlad.client.ui.views.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tarlad.client.databinding.FragmentLoginBinding
import com.tarlad.client.helpers.bindText
import com.tarlad.client.ui.views.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoginFragment: Fragment() {

    private val vm: AuthViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentLoginBinding =
            FragmentLoginBinding.inflate(inflater, container, false)

        binding.registerBtn.setOnClickListener {
            vm.switchToRegistration()
        }
        binding.loginBtn.setOnClickListener {
            vm.login()
        }

        binding.editEmail.bindText(this, vm.loginEmail)
        binding.editPassword.bindText(this, vm.loginPassword)

        return binding.root
    }
}
