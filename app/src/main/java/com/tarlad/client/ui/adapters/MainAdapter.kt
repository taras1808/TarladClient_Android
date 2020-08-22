package com.tarlad.client.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.LastMessage
import kotlinx.android.synthetic.main.item_chat.view.*
import java.util.*
import kotlin.math.absoluteValue

class MainAdapter(
    val chats: SortedSet<LastMessage> = sortedSetOf(Comparator { o1, o2 ->
        o2.message.time.compareTo(
            o1.message.time
        )
    }), var listener: ((chat: Chat) -> Unit)? = null
) :
    RecyclerView.Adapter<MainAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return UserViewHolder(
            view,
            listener
        )
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val chat = chats.toList()[position]
        holder.chat = chat
    }

    fun add(messages: List<LastMessage>) {
        messages.forEach { lastMessage ->
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

    class UserViewHolder(val view: View, listener: ((chat: Chat) -> Unit)?) :
        RecyclerView.ViewHolder(view) {

        var chat: LastMessage? = null
            set(value) {
                field = value!!

                view.title.text = "${value.title}"
                view.last_message.text = "${value.message.data}"

                Glide.with(view.context)
                    .load("https://picsum.photos/" + (value.title.hashCode().absoluteValue % 100 + 100))
                    .into(view.chat_image)
            }

        init {
            view.setOnClickListener {
                chat?.let { chat -> listener?.let { it(Chat(chat.id, chat.title)) } }
            }
        }
    }
}