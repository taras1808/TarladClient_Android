package com.tarlad.client.ui.views.chat.details

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.tarlad.client.R
import com.tarlad.client.ui.adapters.DetailsAdapter
import com.tarlad.client.ui.views.chat.participants.ChatAddParticipantsActivity
import kotlinx.android.synthetic.main.activity_chat_details.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class ChatDetailsActivity : AppCompatActivity() {

    private val adapter =
        DetailsAdapter(arrayListOf())

    private val vm by viewModel<ChatDetailsViewModel> { parametersOf(lifecycleScope.id) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_details)
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
//        toolbar.toolbar_title.text = "Details"

        val chatId: Long = intent.getLongExtra("ID",-1L)

        participants.adapter = adapter

        vm.loadUsers(chatId)

        vm.users.observe(this , Observer {
            adapter.users.clear()
            adapter.users.addAll(it)
            adapter.notifyDataSetChanged()
        })

        add_participants.setOnClickListener {
            val intent = Intent(this, ChatAddParticipantsActivity::class.java)
            intent.putExtra("ID", chatId)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }
}
