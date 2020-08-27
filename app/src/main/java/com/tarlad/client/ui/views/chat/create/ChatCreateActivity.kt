package com.tarlad.client.ui.views.chat.create

import android.content.Intent
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
import com.tarlad.client.ui.views.chat.ChatActivity
import kotlinx.android.synthetic.main.activity_chat_create.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class ChatCreateActivity : AppCompatActivity() {

    private val vm by viewModel<ChatCreateViewModel> { parametersOf(lifecycleScope.id) }
    private val adapter =
        UsersAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_create)
        setSupportActionBar(toolbar as Toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP
//        toolbar.toolbar_title.text = getString(R.string.new_chat)

//        observeRefreshing()
        observeError()
        observeUsers()
        observeOpenChat()

        recycler.adapter = adapter

        vm.search("")
        adapter.listener = { vm.search("") }

//        swiperefresh.setOnRefreshListener {
//            vm.refresh()
//        }

        search.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString()
                adapter.data.clear()
                adapter.notifyDataSetChanged()
                adapter.listener = { vm.search(q) }
                vm.searchUsersDisposable?.dispose()
                vm.page = 0
                vm.search(q)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat_create, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.add_chat_ok -> vm.createChat(adapter.selected)
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

    private fun observeOpenChat() {
        vm.openChat.observe(this , Observer {
            if (it != null) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("ID", it.id)
                intent.putExtra("TITLE", it.title)
                startActivity(intent)
                finish()
            }
        })
    }

//    private fun observeRefreshing() {
//        vm.refreshing.observe(this, Observer {
//            swiperefresh.isRefreshing = it
//        })
//    }

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
