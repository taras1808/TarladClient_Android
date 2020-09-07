package com.tarlad.client.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tarlad.client.databinding.ItemChatDetailsBinding
import com.tarlad.client.models.db.User

class ChatDetailsAdapter(
    val users: ArrayList<User>,
    var userId: Long,
    var adminId: Long,
    var listener: ((Long) -> Unit)
) : RecyclerView.Adapter<ChatDetailsAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserViewHolder(ItemChatDetailsBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, adminId == user.id, adminId == userId) { listener(user.id) }
    }

    class UserViewHolder(val binding: ItemChatDetailsBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, isAdmin: Boolean, isControl: Boolean, action: () -> Unit) {
            binding.user = user
            binding.isAdmin = isAdmin
            binding.isControl = isControl && !isAdmin
            binding.settings.setOnClickListener { action() }
            binding.executePendingBindings()
        }
    }
}
