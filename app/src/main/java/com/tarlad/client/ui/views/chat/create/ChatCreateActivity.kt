package com.tarlad.client.ui.views.chat.create

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityChatCreateBinding
import com.tarlad.client.helpers.bindText
import com.tarlad.client.ui.adapters.UsersAdapter
import com.tarlad.client.ui.views.chat.ChatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


class ChatCreateActivity : AppCompatActivity() {

    private val vm by viewModel<ChatCreateViewModel>()
    private val adapter = UsersAdapter(arrayListOf()) { invalidateOptionsMenu() }
    private lateinit var binding: ActivityChatCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP

        binding.search.bindText(this, vm.search)
        binding.toolbarInclude.toolbarTitle.text = getString(R.string.new_chat)

        initRecyclerView()

        observeError()
        observeUsers()
        observeSearch()
        observeOpenChat()
        observeComplete()

        vm.search()
    }

    private fun observeComplete() {
        vm.complete.observe(this, Observer {
            if (it) binding.userRecycler.clearOnScrollListeners()
        })
    }

    private fun observeSearch() {
        vm.search.observe(this, Observer {
            adapter.users.clear()
            vm.page = 0
            vm.searchUsersDisposable?.dispose()
            vm.search()
            initRecyclerView()
        })
    }

    private fun initRecyclerView() {
        binding.userRecycler.adapter = adapter
        binding.userRecycler.clearOnScrollListeners()
        binding.userRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = binding.userRecycler.layoutManager!!.itemCount
                val lastVisibleItem = (binding.userRecycler.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (totalItemCount <= lastVisibleItem + 5)
                    if (!vm.search.value.isNullOrEmpty())
                        vm.search()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (adapter.selected.isNotEmpty())
            menuInflater.inflate(R.menu.menu_confirm, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_confirm -> vm.createChat(adapter.selected)
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeUsers() {
        vm.users.observe(this , Observer {
            adapter.users.removeAll(it)
            adapter.users.addAll(it)
            adapter.notifyDataSetChanged()
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

    private fun observeError() {
        vm.error.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                val snack = Snackbar.make(binding.createChatContainer, it, Snackbar.LENGTH_LONG)
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
