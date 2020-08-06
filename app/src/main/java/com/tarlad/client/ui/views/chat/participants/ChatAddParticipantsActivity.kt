package com.tarlad.client.ui.views.chat.participants

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.ui.adapters.UsersAdapter
import kotlinx.android.synthetic.main.activity_chat_create.*
import kotlinx.android.synthetic.main.toolbar.view.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class ChatAddParticipantsActivity : AppCompatActivity() {

    private val vm by viewModel<ChatAddParticipantsViewModel> { parametersOf(lifecycleScope.id) }
    private val adapter =
        UsersAdapter(arrayListOf())
    private var chatId: Long = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_create)
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
        toolbar.toolbar_title.text = getString(R.string.add_participants)

        observeError()
        observeUsers()

        chatId = intent.getLongExtra("ID",-1L)

        recycler.adapter = adapter

        vm.search("", chatId)
        adapter.listener = { vm.search("", chatId) }

        search.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString()
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                adapter.listener = { vm.search(q, chatId) }
                vm.searchUsersDisposable?.dispose()
                vm.page = -1
                vm.search(q, chatId)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        vm.success.observe(this , Observer {
            if (it) finish()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat_create, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.add_chat_ok -> vm.addParticipants(chatId, adapter.selected)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun observeUsers() {
        vm.users.observe(this , Observer {
            if (adapter.data.containsAll(it)) {
                adapter.listener = {}
                return@Observer
            }
            val count = adapter.data.size
            adapter.data.addAll(it)
            adapter.notifyItemRangeInserted(count, count + it.size)
        })
    }

    private fun observeError() {
        vm.error.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                val snack = Snackbar.make(create_chat_container, it, Snackbar.LENGTH_LONG)
                snack.setBackgroundTint(
                    ContextCompat.getColor(applicationContext, R.color.colorError)
                )
                snack.show()
            }
        })
        vm.error.value = null
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }
}
