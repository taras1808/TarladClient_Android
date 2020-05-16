package com.tarlad.client.ui.views.addChat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.models.User
import kotlinx.android.synthetic.main.add_chat_item.view.*

class UsersAdapter(val data: ArrayList<User>) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    val selected = ArrayList<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.add_chat_item, parent, false)
        return UserViewHolder(view) {user, isChecked ->
            if (user != null) {
                if(isChecked)
                    selected.add(user)
                else
                    selected.remove(user)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = data[position]
        holder.user = user
        holder.itemView.check_box.isChecked = selected.contains(user)
    }

    class UserViewHolder(val view: View, listener: (user: User?, isChecked: Boolean) -> Unit): RecyclerView.ViewHolder(view) {
        var user: User? = null
            set(value) {
                field = value
                view.nickname.text = value?.nickname
                view.full_name.text = "${value?.name} ${value?.surname}"
                Glide.with(view.context).load("http://lorempixel.com/250/250").into(view.imageURL)
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