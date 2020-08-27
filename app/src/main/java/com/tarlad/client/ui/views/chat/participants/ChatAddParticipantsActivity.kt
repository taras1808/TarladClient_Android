package com.tarlad.client.ui.views.chat.participants

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
import com.tarlad.client.databinding.ActivityChatParticipantsBinding
import com.tarlad.client.ui.adapters.UsersAdapter
import kotlinx.android.synthetic.main.activity_chat_participants.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class ChatAddParticipantsActivity : AppCompatActivity() {

    private val vm by viewModel<ChatAddParticipantsViewModel> { parametersOf(lifecycleScope.id) }
    private val adapter = UsersAdapter(arrayListOf()) { invalidateOptionsMenu() }
    private var chatId: Long = -1
    private lateinit var binding: ActivityChatParticipantsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatId = intent.getLongExtra("ID",-1L)
        vm.title.value = getString(R.string.add_participants)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_participants)
        binding.vm = vm
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP

        initRecyclerView()

        observeError()
        observeUsers()
        observeSearch()
        observeFinish()
        observeComplete()

        vm.search(chatId)

    }

    private fun observeComplete() {
        vm.complete.observe(this , Observer {
            if (it) binding.participantsSearchRecycler.clearOnScrollListeners()
        })
    }

    private fun observeFinish() {
        vm.success.observe(this , Observer {
            if (it) finish()
        })
    }

    private fun observeSearch() {
        vm.search.observe(this, Observer {
            adapter.users.clear()
            vm.page = 0
            vm.searchUsersDisposable?.dispose()
            vm.search(chatId)
            initRecyclerView()
        })
    }

    private fun initRecyclerView() {
        binding.participantsSearchRecycler.adapter = adapter
        binding.participantsSearchRecycler.clearOnScrollListeners()
        binding.participantsSearchRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = binding.participantsSearchRecycler.layoutManager!!.itemCount
                val lastVisibleItem = (binding.participantsSearchRecycler.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (totalItemCount <= lastVisibleItem + 5)
                    if (!vm.search.value.isNullOrEmpty())
                        vm.search(chatId)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (adapter.selected.isNotEmpty())
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
            adapter.users.clear()
            adapter.users.addAll(it)
            adapter.notifyDataSetChanged()
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
