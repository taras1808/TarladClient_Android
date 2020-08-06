package com.tarlad.client.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.dto.LastMessage
import kotlinx.android.synthetic.main.item_chat.view.*
import kotlin.math.absoluteValue

class MainAdapter(val chats: ArrayList<LastMessage>, private val listener: (chat: Chat) -> Unit) : RecyclerView.Adapter<MainAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return UserViewHolder(
            view,
            listener
        )
    }

    override fun getItemCount(): Int = chats.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val chat = chats[position]
        holder.chat = chat
    }

    class UserViewHolder(val view: View, listener: (chat: Chat) -> Unit): RecyclerView.ViewHolder(view) {

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
            view.setOnClickListener{
                chat?.let { listener(Chat(it.id, it.title)) }
            }
        }
    }
}