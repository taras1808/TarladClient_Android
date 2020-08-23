package com.tarlad.client.ui.views.chat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityChatBinding
import com.tarlad.client.ui.adapters.MessageItemAnimator
import com.tarlad.client.ui.adapters.MessagesAdapter
import com.tarlad.client.ui.views.chat.details.ChatDetailsActivity
import kotlinx.android.synthetic.main.activity_chat.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class ChatActivity : AppCompatActivity() {

    private val vm by viewModel<ChatViewModel> { parametersOf(lifecycleScope.id) }
    private val adapter = MessagesAdapter()
    private var chatId: Long = -1

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra("TITLE")
        chatId = intent.getLongExtra("ID",-1L)
        vm.title.value = title

        val binding: ActivityChatBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_chat)
        binding.vm = vm
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP


        initRecyclerView()
        initAdapter()


        observeError()
        observeUsers()
        observeMessages()


        vm.chatId = chatId
        vm.getUsers(chatId)
        vm.observeMessages(chatId)
        vm.getMessages(chatId)
    }

    private fun observeUsers() {
        vm.users.observe(this , Observer {
            adapter.users.clear()
            adapter.users.addAll(it)

            val users = adapter.users.filter { e -> e.id != vm.appSession.userId }

            vm.title.value =
                if (users.isNotEmpty())
                    users.map { e -> e.nickname }
                        .reduceRight { s, acc -> "$s, $acc" }
                else ""
        })
    }

    private fun observeMessages() {
        vm.messages.observe(this, Observer { pair ->
            val action = pair.first
            val messages = pair.second
            when (action) {
                Messages.ADD -> adapter.add(messages)
                Messages.REMOVE -> adapter.remove(messages)
                Messages.DELETE -> adapter.delete(messages)
                Messages.UPDATE -> adapter.update(messages)
                Messages.REPLACE -> adapter.replace(messages)
                Messages.COMPLETE -> messages_recycler.clearOnScrollListeners()
                Messages.SEND -> {
                    adapter.add(messages)
                    messages_recycler.smoothScrollToPosition(0)
                }
            }
        })
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.open_chat_details -> {
                val intent = Intent(this, ChatDetailsActivity::class.java)
                intent.putExtra("ID", chatId)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    private fun initRecyclerView() {
        messages_recycler.adapter = adapter
        messages_recycler.itemAnimator = MessageItemAnimator()
        messages_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = messages_recycler.layoutManager!!.itemCount
                val lastVisibleItem = (messages_recycler.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (totalItemCount <= lastVisibleItem + 5)
                    vm.getMessages(chatId)
            }
        })
    }

    private fun initAdapter() {
        adapter.userId = vm.appSession.userId ?: -1
        adapter.listener = { message -> vm.deleteMessage(message) }
    }
}
