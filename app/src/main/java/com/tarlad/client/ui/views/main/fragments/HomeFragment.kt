package com.tarlad.client.ui.views.main.fragments

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarlad.client.R
import com.tarlad.client.databinding.FragmentHomeBinding
import com.tarlad.client.models.db.Chat
import com.tarlad.client.ui.adapters.MainAdapter
import com.tarlad.client.ui.views.main.Chats
import com.tarlad.client.ui.views.main.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    val vm: MainViewModel by activityViewModels()
    val adapter = MainAdapter()

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
        observeChats()

        vm.getChats()
        vm.observeChats()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        vm.title.value = getString(R.string.app_name)
    }

    private fun observeChats() {
        vm.chats.observe(requireActivity(), Observer { list ->
            list.forEach { pair ->
                val action = pair.first
                val messages = pair.second
                when (action) {
                    Chats.ADD -> adapter.add(messages)
                    Chats.DELETE -> adapter.delete(messages)
                    Chats.COMPLETE -> binding.chatsRecycler.clearOnScrollListeners()
                }
            }
            vm.chats.value!!.clear()
        })
    }
    
    private fun initRecyclerView() {
        binding.chatsRecycler.adapter = adapter
        adapter.listener = { chat: Chat -> vm.openChat.value = chat }
        binding.chatsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = chats_recycler.layoutManager!!.itemCount
                val layoutManager = chats_recycler.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (totalItemCount <= lastVisibleItem + 5)
                    vm.getChats()
            }
        })
    }
}
