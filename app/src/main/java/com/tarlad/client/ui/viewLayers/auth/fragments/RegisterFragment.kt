package com.tarlad.client.ui.viewLayers.auth.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.tarlad.client.R
import com.tarlad.client.states.AuthState
import com.tarlad.client.states.RegisterEmail
import com.tarlad.client.ui.viewLayers.auth.AuthViewModel
import kotlinx.android.synthetic.main.fragment_register.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RegisterFragment: Fragment(R.layout.fragment_register) {
    private val vm: AuthViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeEmailState()


        edit_email.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()

                if (!vm.isEmailMatchRegex(email) || email.isEmpty())
                    vm.registerEmail.value = RegisterEmail.Empty
                else
                    vm.checkEmail(email)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        login_btn.setOnClickListener {
            vm.state.value = AuthState.Login
        }

        register_btn.setOnClickListener {
            vm.register(edit_email.text.toString(),
                edit_password.text.toString(),
                edit_name.text.toString(),
                edit_surname.text.toString())
        }
    }

    private fun observeEmailState() {
        vm.registerEmail.observe(requireActivity(), Observer {
            when(it) {
                RegisterEmail.Empty -> {
                    email_progress.visibility = GONE
                    email_status.visibility = GONE
                }
                RegisterEmail.Loading -> {
                    email_progress.visibility = VISIBLE
                    email_status.visibility = GONE
                }
                RegisterEmail.Ok -> {
                    email_progress.visibility = GONE
                    email_status.visibility = VISIBLE
                    email_status.setImageResource(R.drawable.ic_done)
                    email_status.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green))
                }
                RegisterEmail.Error -> {
                    email_progress.visibility = GONE
                    email_status.visibility = VISIBLE
                    email_status.setImageResource(R.drawable.ic_error_outline)
                    email_status.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }
        })
    }
}