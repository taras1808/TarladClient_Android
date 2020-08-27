package com.tarlad.client.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.models.db.User
import kotlinx.android.synthetic.main.item_chat_details.view.*
import kotlin.math.absoluteValue

class DetailsAdapter(
    val users: ArrayList<User>
) : RecyclerView.Adapter<DetailsAdapter.UserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_details, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.user = user
    }

    class UserViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        var user: User? = null
            set(value) {
                field = value
                view.nickname.text = value?.nickname
                view.full_name.text = "${value?.name} ${value?.surname}"
                Glide.with(view.context)
                    .load("https://picsum.photos/" + (user?.nickname.hashCode().absoluteValue % 100 + 100))
                    .into(view.imageURL)
            }
    }
}
