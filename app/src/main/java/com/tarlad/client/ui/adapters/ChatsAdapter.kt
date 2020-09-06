package com.tarlad.client.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tarlad.client.databinding.ItemChatBinding
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.LastMessage
import java.util.*

class ChatsAdapter(
    val chats: SortedSet<LastMessage>, var listener: ((chat: Chat) -> Unit)? = null
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ChatViewHolder(
            ItemChatBinding.inflate(
                layoutInflater,
                parent,
                false
            ), listener
        )
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats.toList()[position]
        holder.bind(chat)
    }

    fun add(messages: List<LastMessage>) {
        for (lastMessage in messages) {
            val posRemove = chats.indexOfFirst { e -> e.id == lastMessage.id }
            chats.removeAll { e -> e.id == lastMessage.id }
            chats.add(lastMessage)
            val posAdd = chats.indexOf(lastMessage)
            if (posRemove == posAdd)
                notifyItemChanged(posAdd)
            else
                when (posRemove) {
                    -1 -> {
                        notifyItemInserted(posAdd)
                    }
                    else -> {
                        notifyItemRemoved(posRemove)
                        notifyItemInserted(posAdd)
                    }
                }
        }
    }

    fun delete(messages: List<LastMessage>) {
        messages.forEach { lastMessage ->
            val posRemove = chats.indexOfFirst { e -> e.id == lastMessage.id }
            if (posRemove != -1) {
                chats.removeAll { e -> e.id == lastMessage.id }
                notifyItemRemoved(posRemove)
            }
        }
    }

    class ChatViewHolder(
        private val binding: ItemChatBinding,
        private val listener: ((chat: Chat) -> Unit)?
    ) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(lastMessage: LastMessage) {
            binding.message = "${lastMessage.users.find { e -> e.id == lastMessage.message.userId}?.nickname ?: "you"}: ${lastMessage.message.data}"
            binding.title = lastMessage.title
            binding.imageUrl = lastMessage.users.find { e -> e.id == lastMessage.message.userId}?.imageURL ?: ""
            binding.root.setOnClickListener {
                listener?.let { it(Chat(lastMessage.id, lastMessage.title, lastMessage.userId)) }
            }
            binding.executePendingBindings()
        }
    }
}
