package com.tarlad.client.ui.views.addChat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tarlad.client.R
import com.tarlad.client.models.User
import kotlinx.android.synthetic.main.add_chat_item.view.*

class UsersAdapter(val data: List<User>) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

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
        holder.user = data[position]
    }

    class UserViewHolder(val view: View, listener: (user: User?, isChecked: Boolean) -> Unit): RecyclerView.ViewHolder(view) {
        var user: User? = null
            set(value) {
                field = value
                view.title.text = value?.name
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