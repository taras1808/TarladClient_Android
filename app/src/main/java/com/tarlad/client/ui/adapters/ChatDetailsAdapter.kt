package com.tarlad.client.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tarlad.client.databinding.ItemChatDetailsBinding
import com.tarlad.client.models.db.User

class ChatDetailsAdapter(
    val users: ArrayList<User>,
    var userId: Long? = null,
    var id: Long = -1,
    var listener: ((Long) -> Unit)? = null
) : RecyclerView.Adapter<ChatDetailsAdapter.UserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserViewHolder(ItemChatDetailsBinding.inflate(layoutInflater, parent, false), listener)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, id == user.id, id == userId)
    }

    class UserViewHolder(val binding: ItemChatDetailsBinding, val listener: ((Long) -> Unit)?): RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User, isAdmin: Boolean, isControl: Boolean) {
            binding.user = user
            binding.isAdmin = isAdmin
            binding.isControl = isControl && !isAdmin
            binding.settings.setOnClickListener {
                listener?.let { it(user.id) }
            }
            binding.executePendingBindings()
        }
    }
}
