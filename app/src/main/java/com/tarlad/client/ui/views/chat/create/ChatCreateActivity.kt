package com.tarlad.client.ui.views.chat.create

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityChatCreateBinding
import com.tarlad.client.ui.adapters.UsersAdapter
import com.tarlad.client.ui.views.chat.ChatActivity
import kotlinx.android.synthetic.main.activity_chat_create.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class ChatCreateActivity : AppCompatActivity() {

    private val vm by viewModel<ChatCreateViewModel> { parametersOf(lifecycleScope.id) }
    private val adapter = UsersAdapter(arrayListOf()) { invalidateOptionsMenu() }
    private lateinit var binding: ActivityChatCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_create)
        binding.vm = vm
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP

        vm.toolbarTitle.value = getString(R.string.new_chat)

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
