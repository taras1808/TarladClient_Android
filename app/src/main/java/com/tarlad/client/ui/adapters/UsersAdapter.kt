package com.tarlad.client.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.models.db.User
import kotlinx.android.synthetic.main.item_chat_create.view.*
import kotlin.math.absoluteValue

class UsersAdapter(
    val data: ArrayList<User>,
    var listener: () -> Unit = {}
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    val selected = ArrayList<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_create, parent, false)
        return UserViewHolder(view) { user, isChecked ->
            if (user != null) {
                if (isChecked)
                    selected.add(user.id)
                else
                    selected.remove(user.id)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = data[position]
        if (position == data.size - 1) listener()
        holder.user = user
        holder.itemView.check_box.isChecked = selected.contains(user.id)
    }

    class UserViewHolder(val view: View, listener: (user: User?, isChecked: Boolean) -> Unit): RecyclerView.ViewHolder(view) {
        var user: User? = null
            @SuppressLint("SetTextI18n")
            set(value) {
                field = value
                view.nickname.text = value?.nickname
                view.full_name.text = "${value?.name} ${value?.surname}"
                Glide.with(view.context)
                    .load("https://picsum.photos/" + (user?.nickname.hashCode().absoluteValue % 100 + 100))
                    .into(view.imageURL)
            }
        init {
            view.setOnClickListener{
                view.check_box.run {
                    isChecked = !isChecked
                    listener(user, isChecked)
                }
            }
        }
    }
}
