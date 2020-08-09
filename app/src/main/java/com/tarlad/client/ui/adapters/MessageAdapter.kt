package com.tarlad.client.ui.adapters

import android.opengl.Visibility
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import kotlinx.android.synthetic.main.message_from_me.view.*
import kotlinx.android.synthetic.main.message_to_me.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.absoluteValue


class MessagesAdapter(
    val pairs: HashMap<String, List<Message>> = hashMapOf(),
    val messages: SortedSet<Message> = sortedSetOf(Comparator { o1, o2 -> o2.time.compareTo(o1.time) }),
    val users: ArrayList<User> = arrayListOf(),
    var userId: Long = -1,
    var listener: ((id: Long) -> Unit)? = {}
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

//    private var mOnLongClickItemListener: OnLongClickItemListener? = null
//
//    fun setOnLongItemClickListener(onLongClickItemListener: OnLongClickItemListener?) {
//        mOnLongClickItemListener = onLongClickItemListener
//    }
//
//    interface OnLongClickItemListener {
//        fun onLongClickItem(v: View?, position: Int): Boolean
//    }


    enum class MessagesAdapter {
        FROM, TO
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            MessagesAdapter.FROM.ordinal ->
                FromViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.message_from_me, parent, false), users, listener
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
                || messages.toList()[position - 1].time - message.time > 3_600_000
        holder.withMargin =
            (position + 1 < messages.size && message.time - messages.toList()[position + 1].time > 60_000)
        holder.showDateTime =
            position == messages.size - 1 || message.time - messages.toList()[position + 1].time > 3_600_000
        holder.bind(message)

//        if (holder is FromViewHolder)
//            holder.itemView.message_block_from
//                .setOnLongClickListener { v -> mOnLongClickItemListener?.onLongClickItem(v, position) ?: false }
    }

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var showNickname: Boolean = true
        var showImage: Boolean = true
        var withMargin: Boolean = true
        var showDateTime: Boolean = true

        abstract fun bind(value: Message)

        fun formatToYesterdayOrToday(date: Date): String {
            val calendar = Calendar.getInstance()
            calendar.time = date
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DATE, -1)
            val lastWeek = Calendar.getInstance()
            lastWeek.add(Calendar.DATE, -7)
            val timeFormatter: DateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            return if (calendar[Calendar.YEAR] == today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]
            ) {
                "Today " + timeFormatter.format(date)
            } else if (calendar[Calendar.YEAR] == yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR]
            ) {
                "Yesterday " + timeFormatter.format(date)
            } else {
                if (calendar[Calendar.DAY_OF_YEAR] > lastWeek[Calendar.DAY_OF_YEAR])
                    SimpleDateFormat("EEEE HH:mm", Locale.getDefault()).format(date)
                else
                    SimpleDateFormat("dd MMMM YYYY HH:mm", Locale.getDefault()).format(date)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    class FromViewHolder(val view: View, val users: List<User>, listener: ((id: Long) -> Unit)?) : ViewHolder(view) {

        var message: Message? = null

        override fun bind(value: Message) {

            message = value

            val scale = view.context.resources.displayMetrics.density

            view.message_from.text = value.data

            if (showDateTime) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    view.datetime_from.text = formatToYesterdayOrToday(Date(value.time))
//                        ZonedDateTime.ofInstant(Date(value.time).toInstant(), ZoneId.systemDefault())
//                            .format(
//                                DateTimeFormatter.ofPattern("E HH:mm")
//                            )
//                    )
                } else {
                    view.datetime_from.text = formatToYesterdayOrToday(Date(value.time))
//                        SimpleDateFormat("E HH:mm", Locale.getDefault()).format(Date(value.time))
//                    )
                }
                view.datetime_from.visibility = View.VISIBLE
            } else {
                view.datetime_from.visibility = View.GONE
            }

            if ((withMargin || showNickname) && !showDateTime)
                if (showNickname)
                    view.message_block_from.layoutParams =
                        (view.message_block_from.layoutParams as ViewGroup.MarginLayoutParams)
                            .apply { setMargins(0, (20.0 * scale + 0.5).toInt(), 0, 0) }
                else
                    view.message_block_from.layoutParams =
                        (view.message_block_from.layoutParams as ViewGroup.MarginLayoutParams)
                            .apply { setMargins(0, (12.0 * scale + 0.5).toInt(), 0, 0) }
            else
                view.message_block_from.layoutParams =
                    (view.message_block_from.layoutParams as ViewGroup.MarginLayoutParams)
                        .apply { setMargins(0, (4.0 * scale + 0.5).toInt(), 0, 0) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                view.time_from.text =
                    ZonedDateTime.ofInstant(Date(value.time).toInstant(), ZoneId.systemDefault())
                        .format(
                            DateTimeFormatter.ofPattern("HH:mm")
                        )
            } else {
                view.time_from.text =
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(value.time))
            }

        }

        init {
            view.message_block.setOnCreateContextMenuListener { menu, _, _ ->
                MenuInflater(view.context).inflate(R.menu.context_menu_messages, menu)
                menu.forEach {
                    it.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_delete_message -> {
                                message?.let { m ->
                                    listener?.let {
                                        it(m.id)
                                    }
                                }
                            }
                            else -> {}
                        }
                        true
                    }
                }
            }
        }
    }

    class ToViewHolder(val view: View, val users: List<User>) : ViewHolder(view) {

        override fun bind(value: Message) {
            val scale = view.context.resources.displayMetrics.density

            if (showNickname || showDateTime) {
                view.nickname.text = users.find { user -> user.id == value.userId }?.nickname
                view.nickname.visibility = View.VISIBLE
            } else {
                view.nickname.visibility = View.GONE
            }

            if (showDateTime) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    view.datetime_to.text = formatToYesterdayOrToday(Date(value.time))
//                        ZonedDateTime.ofInstant(
//                            Date(value.time).toInstant(),
//                            ZoneId.systemDefault()
//                        )
//                            .format(
//                                DateTimeFormatter.ofPattern("E HH:mm")
//                            )
//                    )
                } else {
                    view.datetime_to.text = formatToYesterdayOrToday(Date(value.time))
//                        SimpleDateFormat("E HH:mm", Locale.getDefault()).format(Date(value.time))
//                    )
                }
                view.datetime_to.visibility = View.VISIBLE
            } else {
                view.datetime_to.visibility = View.GONE
            }

            if ((withMargin || showNickname) && !showDateTime)
                if (showNickname)
                    view.message_block_to.layoutParams =
                        (view.message_block_to.layoutParams as ViewGroup.MarginLayoutParams)
                            .apply { setMargins(0, (20.0 * scale + 0.5).toInt(), 0, 0) }
                else
                    view.message_block_to.layoutParams =
                        (view.message_block_to.layoutParams as ViewGroup.MarginLayoutParams)
                            .apply { setMargins(0, (12.0 * scale + 0.5).toInt(), 0, 0) }
            else
                view.message_block_to.layoutParams =
                    (view.message_block_to.layoutParams as ViewGroup.MarginLayoutParams)
                        .apply { setMargins(0, (4.0 * scale + 0.5).toInt(), 0, 0) }

            view.message_to.text = value.data

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                view.time_to.text =
                    ZonedDateTime.ofInstant(Date(value.time).toInstant(), ZoneId.systemDefault())
                        .format(
                            DateTimeFormatter.ofPattern("HH:mm")
                        )
            } else {
                view.time_to.text =
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(value.time))
            }

            if (showImage) {
                Glide.with(view)
                    .load("https://picsum.photos/" + (users.find { it.id == value.userId }?.nickname.hashCode().absoluteValue % 100 + 100))
                    .into(view.imageView)

            }
            else
                view.imageView.setImageDrawable(null);
        }
    }
}

