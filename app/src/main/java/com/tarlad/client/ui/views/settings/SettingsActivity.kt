package com.tarlad.client.ui.views.settings

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivitySettingsBinding
import com.tarlad.client.states.Register
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private val vm: SettingsViewModel by viewModel { parametersOf(lifecycleScope.id) }

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding.vm = vm
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP

        vm.toolbarTitle.value = getString(R.string.settings)

        observeError()
        observeName()
        observeSurname()
        observeNickname()
        observeNicknameState()

        vm.getUser()
    }

    private fun observeName() {
        vm.name.observe(this, {
            invalidateOptionsMenu()

        })
    }

    private fun observeSurname() {
        vm.surname.observe(this, {
            invalidateOptionsMenu()

        })
    }

    private fun observeNickname() {
        vm.nickname.observe(this, {
            val nickname = it.toLowerCase(Locale.getDefault())
            vm.checkNicknameDisposable?.dispose()
            if (nickname.isEmpty())
                vm.nicknameState.value = Register.Empty
            else
                vm.checkNickname(nickname)
        })
    }

    private fun observeError() {
        vm.error.observe(this, {
            val snack = Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
            val color = ContextCompat.getColor(applicationContext, R.color.colorError)
            snack.setBackgroundTint(color)
            snack.show()
        })
    }

    private fun observeNicknameState() {
        vm.nicknameState.observe(this, {
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
                    binding.nicknameStatus.setColorFilter(ContextCompat.getColor(this, R.color.green))
                }
                Register.Error -> {
                    vm.nicknameProgress.value = false
                    vm.nicknameStatus.value = true
                    binding.nicknameStatus.setImageResource(R.drawable.ic_error_outline)
                    binding.nicknameStatus.setColorFilter(ContextCompat.getColor(this, R.color.colorError))
                }
                else -> {}
            }
            invalidateOptionsMenu()
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (!vm.name.value.isNullOrEmpty()
            && !vm.surname.value.isNullOrEmpty()
            && ((vm.nicknameState.value == Register.Ok && vm.nickname.value != vm.oldNickname)
                    || (vm.nicknameState.value == Register.Empty && vm.nickname.value == vm.oldNickname))
            && (vm.nickname.value != vm.oldNickname || vm.surname.value != vm.oldSurname || vm.name.value != vm.oldName))
            menuInflater.inflate(R.menu.menu_confirm, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_confirm -> vm.updateUser()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}