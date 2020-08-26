package com.tarlad.client.ui.views.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.tarlad.client.R
import com.tarlad.client.databinding.FragmentLoginBinding
import com.tarlad.client.ui.views.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class LoginFragment: Fragment() {

    private val vm: AuthViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentLoginBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this
        return binding.root
    }
}
