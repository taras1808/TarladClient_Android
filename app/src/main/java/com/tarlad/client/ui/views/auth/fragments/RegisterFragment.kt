package com.tarlad.client.ui.views.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.tarlad.client.R
import com.tarlad.client.databinding.FragmentRegisterBinding
import com.tarlad.client.helpers.bindText
import com.tarlad.client.states.Register
import com.tarlad.client.ui.views.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

class RegisterFragment: Fragment() {

    private val vm: AuthViewModel by sharedViewModel()
    lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)
        binding.loginBtn.setOnClickListener {
            vm.switchToLogin()
        }
        binding.registerBtn.setOnClickListener {
            vm.register()
        }
        binding.editSurname.bindText(this, vm.registerSurname)
        binding.editName.bindText(this, vm.registerName)
        binding.editNickname.bindText(this, vm.registerNickname)
        binding.editConfirmPassword.bindText(this, vm.registerConfirmPassword)
        binding.editPassword.bindText(this, vm.registerPassword)
        binding.editEmail.bindText(this, vm.registerEmail)

        vm.nicknameProgress.observe(viewLifecycleOwner) {
            binding.nicknameProgress.visibility =
                if (it) View.VISIBLE else View.GONE
        }
        vm.nicknameStatus.observe(viewLifecycleOwner) {
            binding.nicknameStatus.visibility =
                if (it) View.VISIBLE else View.GONE
        }
        vm.emailProgress.observe(viewLifecycleOwner) {
            binding.emailProgress.visibility =
                if (it) View.VISIBLE else View.GONE
        }
        vm.emailStatus.observe(viewLifecycleOwner) {
            binding.emailStatus.visibility =
                if (it) View.VISIBLE else View.GONE
        }
        observeEmail()
        observeNickname()
        observeEmailState()
        observeNicknameState()
        return binding.root
    }

    private fun observeEmail() {
        vm.registerEmail.observe(viewLifecycleOwner) {
            val email = it.lowercase(Locale.getDefault())
            vm.checkEmailDisposable?.dispose()
            if (email.isEmpty() || !vm.isEmailMatchRegex(email))
                vm.registerEmailState.value = Register.Empty
            else
                vm.checkEmail(email)
        }
    }

    private fun observeNickname() {
        vm.registerNickname.observe(viewLifecycleOwner) {
            val nickname = it.lowercase(Locale.getDefault())
            vm.checkNicknameDisposable?.dispose()
            if (nickname.isEmpty())
                vm.registerNicknameState.value = Register.Empty
            else
                vm.checkNickname(nickname)
        }
    }

    private fun observeEmailState() {
        vm.registerEmailState.observe(viewLifecycleOwner) {
            when(it) {
                Register.Empty -> {
                    vm.emailProgress.value = false
                    vm.emailStatus.value = false
                }
                Register.Loading -> {
                    vm.emailProgress.value = true
                    vm.emailStatus.value = false
                }
                Register.Ok -> {
                    vm.emailProgress.value = false
                    vm.emailStatus.value = true
                    binding.emailStatus.setImageResource(R.drawable.ic_done)
                    binding.emailStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green))
                }
                Register.Error -> {
                    vm.emailProgress.value = false
                    vm.emailStatus.value = true
                    binding.emailStatus.setImageResource(R.drawable.ic_error_outline)
                    binding.emailStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorError))
                }
                else -> {}
            }
        }
    }

    private fun observeNicknameState() {
        vm.registerNicknameState.observe(viewLifecycleOwner) {
            when(it) {
                Register.Empty -> {
                    vm.nicknameProgress.value = false
                    vm.nicknameStatus.value = false
                }
                Register.Loading -> {
                    vm.nicknameProgress.value = true
                    vm.nicknameStatus.value = false
                }
                Register.Ok -> {
                    vm.nicknameProgress.value = false
                    vm.nicknameStatus.value = true
                    binding.nicknameStatus.setImageResource(R.drawable.ic_done)
                    binding.nicknameStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green))
                }
                Register.Error -> {
                    vm.nicknameProgress.value = false
                    vm.nicknameStatus.value = true
                    binding.nicknameStatus.setImageResource(R.drawable.ic_error_outline)
                    binding.nicknameStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorError))
                }
                else -> {}
            }
        }
    }
}
