package com.tarlad.client.ui.views.main.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarlad.client.R
import com.tarlad.client.databinding.FragmentHomeBinding
import com.tarlad.client.ui.adapters.ChatsAdapter
import com.tarlad.client.enums.Chats
import com.tarlad.client.ui.views.chat.ChatActivity
import com.tarlad.client.ui.views.main.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class HomeFragment : Fragment() {

    val vm: MainViewModel by sharedViewModel()
    lateinit var adapter: ChatsAdapter

    lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this

        initRecyclerView()

        observeMessages()
        observeUsers()
        observeChats()
        observeChatLists()
        observeOpenChat()

        vm.getMessages()
        vm.observeMessages()
        vm.observeChats()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        vm.toolbarTitle.value = getString(R.string.app_name)
    }

    private fun observeMessages() {
        vm.messagesLiveData.observe(viewLifecycleOwner, { list ->
            list.forEach { pair ->
                val action = pair.first
                val messages = pair.second
                messages.forEach { message ->
                    if (!vm.chats.map { e -> e.id }.contains(message.chatId)) {
                        vm.getChat(message.chatId)
                    }

                    if (!vm.users.map { e -> e.id }.contains(message.userId)) {
                        vm.getUser(message.userId)
                    }
                }
                when (action) {
                    Chats.ADD -> adapter.add(messages)
                    Chats.DELETE -> adapter.delete(messages)
                    Chats.COMPLETE -> binding.chatsRecycler.clearOnScrollListeners()
                }
            }
            vm.messagesLiveData.value!!.clear()
        })
    }

    private fun observeUsers() {
        vm.usersLiveDate.observe(viewLifecycleOwner, {
            vm.users.removeAll { e -> e.id == it.id }
            vm.users.add(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun observeChats() {
        vm.chatsLiveDate.observe(viewLifecycleOwner, {
            vm.chats.removeAll { e -> e.id == it.id }
            vm.chats.add(it)
            if (!vm.chatLists.containsKey(it.id)) {
                vm.getChatLists(it.id)
            }
            adapter.notifyDataSetChanged()
        })
    }

    private fun observeChatLists() {
        vm.chatListsLiveDate.observe(viewLifecycleOwner, {
            vm.chatLists.remove(it.first)
            vm.users.removeAll(it.second)
            vm.users.addAll(it.second)
            vm.chatLists[it.first] = it.second.map { e -> e.id }
            adapter.notifyDataSetChanged()
        })
    }

    private fun observeOpenChat() {
        vm.openChat.observe(viewLifecycleOwner, Observer {
            if (it == null) return@Observer
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("ID", it)
            startActivity(intent)
            vm.openChat.value = null
        })
    }

    private fun initRecyclerView() {
        adapter = ChatsAdapter(vm.messages, vm.users, vm.chats, vm.chatLists, vm.appSession.userId!!) { chatId: Long -> vm.openChat.value = chatId }
        binding.chatsRecycler.adapter = adapter
        binding.chatsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = chats_recycler.layoutManager!!.itemCount
                val layoutManager = chats_recycler.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (totalItemCount <= lastVisibleItem + 5)
                    vm.getMessages()
            }
        })
    }
}
