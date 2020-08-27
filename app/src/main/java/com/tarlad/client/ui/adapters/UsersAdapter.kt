package com.tarlad.client.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tarlad.client.databinding.ItemChatCreateBinding
import com.tarlad.client.models.db.User

class UsersAdapter(val users: ArrayList<User>, val listener: () -> Unit) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    val selected = ArrayList<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserViewHolder(ItemChatCreateBinding.inflate(layoutInflater, parent, false)) { user ->
            if (!selected.contains(user.id))
                selected.add(user.id)
            else
                selected.remove(user.id)
            listener()
        }
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user, selected.contains(user.id))
    }

    class UserViewHolder(val binding: ItemChatCreateBinding, val listener: (user: User) -> Unit): RecyclerView.ViewHolder(binding.root) {

        var selected: Boolean = false

        fun bind(user: User, selected: Boolean) {
            binding.user = user
            binding.selected = selected
            this.selected = selected
            binding.root.setOnClickListener {
                this.selected = !this.selected
                binding.selected = this.selected
                listener(user)
                binding.executePendingBindings()
            }
            binding.executePendingBindings()
        }
    }
}
