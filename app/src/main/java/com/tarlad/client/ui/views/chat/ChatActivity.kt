package com.tarlad.client.ui.views.chat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.ui.adapters.MessagesAdapter
import com.tarlad.client.ui.views.chat.details.ChatDetailsActivity
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.toolbar.view.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.lang.Exception
import java.util.*


class ChatActivity : AppCompatActivity() {

    private val vm by viewModel<ChatViewModel> { parametersOf(lifecycleScope.id) }
    private val adapter = MessagesAdapter()
    private var chatId: Long = -1

    var loading = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar as Toolbar)

        val title = intent.getStringExtra("TITLE")
        chatId = intent.getLongExtra("ID",-1L)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
        toolbar.toolbar_title.text = title


        messages_recycler.adapter = adapter

        messages_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = messages_recycler.layoutManager?.itemCount ?: 0
                val lastVisibleItem = (messages_recycler.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (!loading && totalItemCount <= lastVisibleItem + 5) {
                    vm.loadOldMessages(chatId)
                    loading = true
                }
            }
        })

        vm.complete.observe(this, Observer {
            if (it) messages_recycler.clearOnScrollListeners()
        })

        vm.messages.observe(this , Observer { pair ->

            println(pair)


                val id = pair.first
                val messages = pair.second

                val w = messages.lastOrNull()?.time
                val v = adapter.messages.firstOrNull()?.time

                if (w ?: 0 > v ?: 0 && messages.size == 5) {
                    vm.loadNewMessages(chatId)
                }

                if (!messages.containsAll(adapter.pairs[id].orEmpty())) {


                    val toRemove = adapter.pairs[id].orEmpty().subtract(messages)
                    val toAdd = messages.subtract(adapter.pairs[id].orEmpty())


                    toRemove.forEach {
                        if (adapter.messages.contains(it)
                            && adapter.pairs.map { e -> e.value.contains(it) }.count { e -> e } != 2
                        ) {
                            val pos = adapter.messages.toList().indexOf(it)
                            adapter.messages.remove(it)
                            adapter.notifyItemRemoved(pos)

                            if (pos > 0 && pos < adapter.messages.size) {
                                val next = adapter.messages.toList()[pos - 1]
                                val prev = adapter.messages.toList()[pos]

                                if (it.userId == prev.userId && next.userId != it.userId)
                                    adapter.notifyItemChanged(pos)

                                if (next.time - prev.time > 60_000 && next.userId == prev.userId || prev.userId != it.userId)
                                    adapter.notifyItemChanged(pos - 1)
                            }
                        }
                    }

                    toAdd.forEach {
                        if (!adapter.messages.contains(it)) {
                            adapter.messages.add(it)
                            val pos = adapter.messages.toList().indexOf(it)
                            adapter.notifyItemInserted(pos)

                            adapter.notifyItemChanged(pos + 1)
                            adapter.notifyItemChanged(pos - 1)
                        }
                    }

                    adapter.pairs[id] = messages


                } else if (!adapter.pairs.containsKey(id)) {

                    adapter.messages.addAll(messages)
                    val posF = adapter.messages.toList().indexOf(messages.firstOrNull())
                    val posL = adapter.messages.toList().indexOf(messages.lastOrNull())

                    messages.forEach {
                        val pos = adapter.messages.toList().indexOf(it)
                        adapter.notifyItemInserted(pos)
                    }

                    adapter.notifyItemChanged(posL + 1)
                    adapter.notifyItemChanged(posF - 1)

                    adapter.pairs[id] = messages

                } else {

                    val toRemove = adapter.pairs[id].orEmpty().subtract(messages)
                    val toAdd = messages.subtract(adapter.pairs[id].orEmpty())

                    toRemove.forEach {
                        if (adapter.messages.contains(it)
                            && adapter.pairs.map { e -> e.value.contains(it) }.count { e -> e } != 2
                        ) {
                            val pos = adapter.messages.toList().indexOf(it)
                            adapter.messages.remove(it)
                            adapter.notifyItemRemoved(pos)

                            if (pos > 0 && pos < adapter.messages.size) {

                                val next = adapter.messages.toList()[pos - 1]
                                val prev = adapter.messages.toList()[pos]

                                if (it.userId == prev.userId && next.userId != it.userId)
                                    adapter.notifyItemChanged(pos)

                                if (next.time - prev.time > 60_000 && next.userId == prev.userId || prev.userId != it.userId)
                                    adapter.notifyItemChanged(pos - 1)
                            }
                        }
                    }

                    toAdd.forEach {
                        if (!adapter.messages.contains(it)) {
                            adapter.messages.add(it)
                            val pos = adapter.messages.toList().indexOf(it)
                            adapter.notifyItemInserted(pos)

                            adapter.notifyItemChanged(pos + 1)
                            adapter.notifyItemChanged(pos - 1)
                        }
                    }

                    adapter.pairs[id] = messages

                }

            loading = false
        })

        vm.users.observe(this , Observer {
            adapter.users.clear()
            adapter.users.addAll(it)


            toolbar.toolbar_title.text = adapter.users.filter { e -> e.id != vm.appSession.userId }.map { e -> e.nickname }.reduceRight { s, acc -> "$s, $acc" }
        })


        adapter.userId = vm.appSession.userId ?: -1
//        adapter.listener = { time -> vm.loadOldMessages(chatId, time) }

        vm.loadUsers(chatId)

        vm.loadNewMessages(chatId)
        vm.loadOldMessages(chatId)



        send_button_chat.setOnClickListener {
            val text = message_to_send.text.toString().trim()
            message_to_send.text.clear()
            if (text.isEmpty()) return@setOnClickListener
            vm.sendMessage(text, chatId)
            messages_recycler.layoutManager?.startSmoothScroll(object : LinearSmoothScroller(it.context) {
                override fun getVerticalSnapPreference(): Int = LinearSmoothScroller.SNAP_TO_END
                override fun getHorizontalSnapPreference(): Int = LinearSmoothScroller.SNAP_TO_END
            }.apply { targetPosition = 0 })
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
}
