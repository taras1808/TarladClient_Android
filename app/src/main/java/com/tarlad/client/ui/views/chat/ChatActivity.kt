package com.tarlad.client.ui.views.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.ui.views.addChat.UsersAdapter
import kotlinx.android.synthetic.main.activity_chat.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ChatActivity : AppCompatActivity() {

    private val vm by viewModel<ChatViewModel> { parametersOf(lifecycleScope.id) }
    private val adapter = MessagesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val chatId = intent.getLongExtra("ID",-1L)

        chat_recycler_view.adapter = adapter

        vm.messages.observe(this , Observer {
            adapter.messages.clear()
            adapter.messages.addAll(it)
            adapter.notifyDataSetChanged()
        })

        vm.loadMessages(chatId)

        send_button_chat.setOnClickListener {
            val text = message_to_send.text.toString().trim()
            message_to_send.text.clear()
            if (text.isEmpty()) return@setOnClickListener
            chat_recycler_view.smoothScrollToPosition(0)
            vm.sendMessage(text, chatId)
        }

        observeError()

    }

    private fun observeError() {
        vm.error.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                val snack = Snackbar.make(chat_container, it, Snackbar.LENGTH_LONG)
                snack.setBackgroundTint(
                    ContextCompat.getColor(applicationContext, R.color.colorError)
                )
                snack.show()
            }
        })
        vm.error.value = null
    }
}
