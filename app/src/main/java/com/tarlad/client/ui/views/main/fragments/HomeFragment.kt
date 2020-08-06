package com.tarlad.client.ui.views.main.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.tarlad.client.R
import com.tarlad.client.models.db.Chat
import com.tarlad.client.ui.adapters.MainAdapter
import com.tarlad.client.ui.views.main.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment(val vm: MainViewModel): Fragment(R.layout.fragment_home) {

    val adapter = MainAdapter(arrayListOf()) { chat: Chat -> vm.openChat.value = chat }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        chats_recycler.adapter = adapter

        observeMessages()

    }

    private fun observeMessages() {
        vm.messages.observe(requireActivity() , Observer {
            if (it == null) return@Observer
            it.forEachIndexed { pos, lastMessage ->
                if (!adapter.chats.contains(lastMessage)) {
                    val index = adapter.chats.indexOfFirst { e -> e.id == lastMessage.id }
                    adapter.chats.removeAll { e -> e.id == lastMessage.id }
                    adapter.chats.add(pos, lastMessage)
                    if (index == pos) adapter.notifyItemChanged(pos)
                    else
                        when (index) {
                            -1 -> {
                                adapter.notifyItemInserted(pos)
                            }
                            else -> {
                                adapter.notifyItemRemoved(index)
                                adapter.notifyItemInserted(pos)
                            }
                        }
                }
            }
            adapter.chats.subtract(it).forEach {
                val pos = adapter.chats.indexOf(it)
                adapter.chats.remove(it)
                adapter.notifyItemRemoved(pos)
            }

        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        vm.title.value = getString(R.string.app_name)
    }
}