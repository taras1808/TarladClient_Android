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
import com.tarlad.client.ui.views.chat.ChatActivity
import com.tarlad.client.ui.views.main.fragments.HomeFragment
import com.tarlad.client.ui.views.main.fragments.ProfileFragment
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel by viewModel { parametersOf(lifecycleScope.id) }
    private val homeFragment: HomeFragment = HomeFragment()
    private val profileFragment: ProfileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = vm
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM

        observeError()
        observeAppState()
        observeFragment()
        observeOpenChat()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_chat_create)?.isVisible = homeFragment.isVisible
        menu?.findItem(R.id.action_logout)?.isVisible = !homeFragment.isVisible
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> showLogoutAlertDialog()
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

    private fun observeOpenChat() {
        vm.openChat.observe(this, Observer {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("ID", it.id)
            intent.putExtra("TITLE", it.title)
            startActivity(intent)
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
            val snack = Snackbar.make(main, it, Snackbar.LENGTH_LONG)
            val color = ContextCompat.getColor(applicationContext, R.color.colorError)
            snack.setBackgroundTint(color)
            snack.show()
        })
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
