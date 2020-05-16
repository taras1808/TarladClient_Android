package com.tarlad.client.ui.views.addChat

import android.graphics.Color
import android.graphics.ColorFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.models.User
import kotlinx.android.synthetic.main.activity_add_chat.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class AddChatActivity : AppCompatActivity() {

    private val vm by viewModel<AddChatViewModel> { parametersOf(lifecycleScope.id) }
    private val adapter = UsersAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_chat)

//        observeRefreshing()
        observeError()
        observeUsers()

        recycler.adapter = adapter

        vm.search("")

//        swiperefresh.setOnRefreshListener {
//            vm.refresh()
//        }

        search.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString()
                vm.search(q)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.add_chat_ok -> onDone()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onDone() {
//        vm.createChat(adapter.selected)
        //TODO
        onBackPressed()
    }

    private fun observeUsers() {
        vm.users.observe(this , Observer {
            adapter.data.clear()
            adapter.data.addAll(it)
            adapter.notifyDataSetChanged()
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
                snack.setBackgroundTint(Color.RED)
                snack.show()
            }
        })
        vm.error.value = null
    }

}
