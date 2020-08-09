package com.tarlad.client.ui.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import kotlinx.android.synthetic.main.message_to_me.view.*
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

class MessagesAdapter(
    val pairs: HashMap<String, List<Message>> = hashMapOf(),
    val messages: SortedSet<Message> = sortedSetOf(Comparator { o1, o2 -> o2.time.compareTo(o1.time) }),
    val users: ArrayList<User> = arrayListOf(),
    var userId: Long = -1,
    var listener: ((time: Long) -> Unit)? = {}
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    var array: ArrayList<ArrayList<Message>> = ArrayList()

    enum class MessagesAdapter {
        FROM, TO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            MessagesAdapter.FROM.ordinal ->
                FromViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.message_from_me, parent, false), users
                )
            else ->
                ToViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.message_to_me, parent, false), users
                )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages.toList()[position].userId == userId) MessagesAdapter.FROM.ordinal else MessagesAdapter.TO.ordinal
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages.toList()[position]
        holder.showNickname =
            position == messages.size - 1 || messages.toList()[position + 1].userId != message.userId
        holder.showImage = position == 0 || messages.toList()[position - 1].userId != message.userId
        holder.withMargin =
            (position + 1 < messages.size && message.time - messages.toList()[position + 1].time > 60_000)
        holder.bind(message)
    }

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var showNickname: Boolean = true
        var showImage: Boolean = true
        var withMargin: Boolean = true

        abstract fun bind(value: Message)
    }

    class FromViewHolder(val view: View, val users: List<User>) : ViewHolder(view) {

        override fun bind(value: Message) {
            val scale = view.context.resources.displayMetrics.density

            view.message.text = value.data

            if (withMargin || showNickname)
                view.message_block.layoutParams =
                    (view.message_block.layoutParams as ViewGroup.MarginLayoutParams)
                        .apply { setMargins(0, (12.0 * scale + 0.5).toInt(), 0, 0) }
            else
                view.message_block.layoutParams =
                    (view.message_block.layoutParams as ViewGroup.MarginLayoutParams)
                        .apply { setMargins(0, (4.0 * scale + 0.5).toInt(), 0, 0) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                view.time.text =
                    ZonedDateTime.ofInstant(Date(value.time).toInstant(), ZoneId.systemDefault())
                        .format(
                            DateTimeFormatter.ofPattern("HH:mm")
                        )
            } else {
                view.time.text =
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(value.time))
            }
        }
    }

    class ToViewHolder(val view: View, val users: List<User>) : ViewHolder(view) {

        override fun bind(value: Message) {
            val scale = view.context.resources.displayMetrics.density

            if (showNickname) {
                view.nickname.text = users.find { user -> user.id == value.userId }?.nickname
                view.nickname.visibility = View.VISIBLE
            } else {
                view.nickname.visibility = View.GONE
            }

            if (withMargin && !showNickname)
                view.message_block.layoutParams =
                    (view.message_block.layoutParams as ViewGroup.MarginLayoutParams)
                        .apply { setMargins(0, (12.0 * scale + 0.5).toInt(), 0, 0) }
            else
                view.message_block.layoutParams =
                    (view.message_block.layoutParams as ViewGroup.MarginLayoutParams)
                        .apply { setMargins(0, (4.0 * scale + 0.5).toInt(), 0, 0) }

            view.message.text = value.data

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                view.time.text =
                    ZonedDateTime.ofInstant(Date(value.time).toInstant(), ZoneId.systemDefault())
                        .format(
                            DateTimeFormatter.ofPattern("HH:mm")
                        )
            } else {
                view.time.text =
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(value.time))
            }

            if (showImage)
                Glide.with(view)
                    .load("https://picsum.photos/" + (users.find { it.id == value.userId }?.nickname.hashCode().absoluteValue % 100 + 100))
                    .into(view.imageView)
        }
    }
}