package com.tarlad.client.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
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

        @SuppressLint("SetTextI18n")
        fun bind(user: User, isAdmin: Boolean, isControl: Boolean, action: () -> Unit) {
            Glide.with(binding.imageURL)
                .load(user.imageURL)
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)
                .into(binding.imageURL)
            binding.nickname.text = user.nickname
            binding.fullName.text = "${user.name} ${user.surname}"
            binding.settings.setOnClickListener { action() }
            binding.settings.visibility = if (isControl && !isAdmin) View.VISIBLE else View.GONE
            binding.admin.visibility = if (isAdmin) View.VISIBLE else View.GONE
        }
    }
}
