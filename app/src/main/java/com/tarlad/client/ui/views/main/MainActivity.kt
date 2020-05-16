package com.tarlad.client.ui.views.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.Observer
import com.tarlad.client.R
import com.tarlad.client.states.AppStates
import com.tarlad.client.ui.views.addChat.AddChatActivity
import com.tarlad.client.ui.views.auth.AuthActivity
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MainActivity : AppCompatActivity() {
    private val vm by viewModel<MainViewModel>{ parametersOf(lifecycleScope.id) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        observeAppState()

        vm.tryLoginWithToken()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = MenuInflater(this)
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout_action -> vm.logout()
            R.id.add_chat_action -> addChat()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observeAppState() {
        vm.appSession.state.observe(this, Observer {
            if (it == AppStates.NotAuthenticated) {
                val intent = Intent(this , AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK + Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        })
    }

    private fun addChat() {
        startActivity(Intent(this , AddChatActivity::class.java))
    }
}
