package com.tarlad.client.ui.views.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityMainBinding
import com.tarlad.client.states.AppStates
import com.tarlad.client.ui.views.chat.create.ChatCreateActivity
import com.tarlad.client.ui.views.auth.AuthActivity
import com.tarlad.client.ui.views.main.fragments.HomeFragment
import com.tarlad.client.ui.views.main.fragments.ProfileFragment
import com.tarlad.client.ui.views.settings.SettingsActivity
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.lang.IllegalArgumentException


class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel by viewModel { parametersOf(lifecycleScope.id) }
    private val homeFragment: HomeFragment = HomeFragment()
    private val profileFragment: ProfileFragment = ProfileFragment()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = vm
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM

        observeError()
        observeAppState()
        observeFragment()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        if (!homeFragment.isVisible) menuInflater.inflate(R.menu.menu_profile, menu)
        else menuInflater.inflate(R.menu.menu_home, menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> showLogoutAlertDialog()
            R.id.action_settings -> openSettingsActivity()
            R.id.action_chat_create -> openChatCreateActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLogoutAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.do_you_really_want_to_logout))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                vm.logout()
                Toast.makeText(this, getString(R.string.bye), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.no, null).show()
    }

    private fun openSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openChatCreateActivity() {
        val intent = Intent(this, ChatCreateActivity::class.java)
        startActivity(intent)
    }

    private fun observeAppState() {
        vm.appSession.state.observe(this, Observer {
            when (it) {
                AppStates.NotAuthenticated -> {
                    val intent = Intent(this, AuthActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> { }
            }
        })
    }

    private fun observeFragment() {
        vm.fragment.observe(this, Observer {
            val selectedFragment = when (it) {
                0 -> homeFragment
                1 -> profileFragment
                else -> throw IllegalArgumentException()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, selectedFragment)
                .commitNow()
            invalidateOptionsMenu()
        })
    }

    private fun observeError() {
        vm.error.observe(this, Observer {
            val snack = Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
            val color = ContextCompat.getColor(applicationContext, R.color.colorError)
            snack.setBackgroundTint(color)
            snack.show()
        })
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
