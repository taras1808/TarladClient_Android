package com.tarlad.client.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.databinding.ItemChatBinding
import com.tarlad.client.helpers.getTitle
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User

class ChatsAdapter(
    val messages: ArrayList<Message>,
    val users: ArrayList<User>,
    private val chats: ArrayList<Chat>,
    private val chatLists: HashMap<Long, List<Long>>,
    private val you: Long,
    var listener: ((chatId: Long) -> Unit)
) : RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ChatViewHolder(
            ItemChatBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = messages[position]
        val user = users.filter { e -> e.id != you }.find { e -> e.id == item.userId}
        val from = if (user == null) "you" else "${user.name} ${user.surname}"
        val data = if (item.type == "text") item.data else "Sent a photo"
        val message = "$from: $data"
        val chat = chats.find { e -> e.id == item.chatId }
        val users = chatLists[chat?.id]?.mapNotNull { e -> users.find { u -> e == u.id } }?.let {
            if (it.size > 1) it.filter { e -> e.id != you } else it
        } ?: arrayListOf()
        val title = getTitle(chat?.title, users, you)

        if (users.size <= 1) {
            val imageUrl = users.firstOrNull()?.imageURL
            holder.bind(message, title, imageUrl) { listener(item.chatId) }
        } else {
            val firstUser = users.find { e -> e.id == item.userId} ?: users[0]
            val imageUrlFront = firstUser.imageURL
            val imageUrlBack = if (firstUser != users[0]) users[0].imageURL else users[1].imageURL
            holder.bind(message, title, imageUrlBack, imageUrlFront) { listener(item.chatId) }
        }

    }

    fun add(list: List<Message>) {
        for (message in list) {
            val posRemove = messages.indexOfFirst { e -> e.chatId == message.chatId }
            messages.removeAll { e -> e.chatId == message.chatId }
            messages.add(message)
            messages.sortByDescending { e -> e.time }
            val posAdd = messages.indexOf(message)
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

    fun delete(list: List<Message>) {
        list.forEach { message ->
            val posRemove = messages.indexOfFirst { e -> e.chatId == message.chatId }
            if (posRemove != -1) {
                messages.removeAll { e -> e.chatId == message.chatId }
                notifyItemRemoved(posRemove)
            }
        }
    }

    class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: String, title: String, imageUrlBack: String?, imageUrlFront: String?, action: () -> Unit) {
            Glide.with(binding.avatarBack)
                .load(imageUrlBack)
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)
                .into(binding.avatarBack)
            Glide.with(binding.avatarFront)
                .load(imageUrlFront)
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)
                .into(binding.avatarFront)
            binding.avatarBack.visibility = View.VISIBLE
            binding.avatarFront.visibility = View.VISIBLE
            binding.avatar.visibility = View.GONE
            binding.message.text = message
            binding.title.text = title
            binding.root.setOnClickListener { action() }
        }

        fun bind(message: String, title: String, imageUrl: String?, action: () -> Unit) {
            Glide.with(binding.avatar)
                .load(imageUrl)
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)
                .into(binding.avatar)
            binding.avatarBack.visibility = View.GONE
            binding.avatarFront.visibility = View.GONE
            binding.avatar.visibility = View.VISIBLE
            binding.message.text = message
            binding.title.text = title
            binding.root.setOnClickListener { action() }
        }
    }
}
