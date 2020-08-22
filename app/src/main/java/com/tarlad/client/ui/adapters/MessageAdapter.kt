package com.tarlad.client.ui.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.forEach
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.databinding.MessageFromMeBinding
import com.tarlad.client.databinding.MessageToMeBinding
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.message_from_me.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.absoluteValue


class MessagesAdapter(
    val messages: ArrayList<Message> = arrayListOf(),
    val users: ArrayList<User> = arrayListOf(),
    var userId: Long = -1,
    var listener: ((id: Long) -> Unit)? = {}
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    enum class MessagesAdapter {
        FROM, TO
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MessagesAdapter.FROM.ordinal ->
                FromViewHolder(
                    MessageFromMeBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    listener
                )
            else ->
                ToViewHolder(
                    MessageToMeBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    users
                )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages.toList()[position].userId == userId)
            MessagesAdapter.FROM.ordinal
        else
            MessagesAdapter.TO.ordinal
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages.toList()[position]
        val showNickname =
            position == messages.size - 1 || messages.toList()[position + 1].userId != message.userId
        val showImage = position == 0 || messages.toList()[position - 1].userId != message.userId
                || messages.toList()[position - 1].time - message.time > 3_600_000
        val withMargin =
            (position + 1 < messages.size && message.time - messages.toList()[position + 1].time > 60_000)
        val showDateTime =
            position == messages.size - 1 || message.time - messages.toList()[position + 1].time > 3_600_000
        holder.bind(message, showImage, showDateTime, withMargin, showNickname)
    }

    fun add(messages: List<Message>) {
        this.messages.addAll(messages)
        this.messages.sortByDescending { it.time }

        messages.forEach {
            val pos = this.messages.indexOf(it)
            if (pos == -1) return

            notifyItemInserted(pos)

            if (pos > 0)
                notifyItemChanged(pos - 1)

            if (pos + 1 < this.messages.size - 1)
                notifyItemChanged(pos + 1)
        }
    }

    fun remove(messages: List<Message>) {
        messages.forEach {

            val count = this.messages.count { e -> e.id == it.id }
            val pos = this.messages.indexOf(it)
            if (pos == -1) return
            this.messages.remove(it)

            if (count == 1) {
                notifyItemRemoved(pos)

                if (pos < this.messages.size - 1)
                    notifyItemChanged(pos)

                if (pos > 0)
                    notifyItemChanged(pos - 1)
            }
        }
    }

    fun delete(messages: List<Message>) {
        messages.forEach { m ->
            val pos = this.messages.indexOfFirst { it.id == m.id }
            if (pos == -1) return
            this.messages.removeAt(pos)
            notifyItemRemoved(pos)

            if (pos < this.messages.size)
                notifyItemChanged(pos)

            if (pos > 0)
                notifyItemChanged(pos - 1)
        }
    }

    fun update(messages: List<Message>) {
        this.messages.addAll(messages)
        this.messages.sortByDescending { it.time }

        messages.forEach {

            val count = this.messages.count { e -> e.id == it.id }

            if (count == 1) {
                val pos = this.messages.indexOf(it)
                if (pos == -1) return
                notifyItemInserted(pos)
                if (pos + 1 < this.messages.size - 1)
                    notifyItemChanged(pos + 1)
                if (pos > 0)
                    notifyItemChanged(pos - 1)
            }
        }
    }

    fun replace(messages: List<Message>) {
        val pos = this.messages.indexOf(messages.first())
        if (pos == -1) return
        this.messages[pos] = messages.last()
        notifyItemChanged(pos)
    }

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(
            message: Message,
            showImage: Boolean,
            showDateTime: Boolean,
            withMargin: Boolean,
            showNickname: Boolean
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    class FromViewHolder(
        private val binding: MessageFromMeBinding,
        private val listener: ((id: Long) -> Unit)?
    ) : ViewHolder(binding.root) {

        override fun bind(
            message: Message,
            showImage: Boolean,
            showDateTime: Boolean,
            withMargin: Boolean,
            showNickname: Boolean
        ) {

            binding.root.message_block.setOnCreateContextMenuListener { menu, _, _ ->
                MenuInflater(binding.root.context).inflate(R.menu.context_menu_messages, menu)
                menu.forEach {
                    it.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_delete_message -> listener?.let { it(message.id) }
                        }
                        true
                    }
                }
            }
            binding.message = message
            binding.showDateTime = showDateTime
            binding.withMargin = withMargin
            binding.showNickname = showNickname
            binding.executePendingBindings()
        }
    }

    class ToViewHolder(
        private val binding: MessageToMeBinding,
        val users: List<User>
    ) : ViewHolder(binding.root) {

        override fun bind(
            message: Message,
            showImage: Boolean,
            showDateTime: Boolean,
            withMargin: Boolean,
            showNickname: Boolean
        ) {
            binding.message = message
            binding.showImage = showImage
            binding.showDateTime = showDateTime
            binding.withMargin = withMargin
            binding.showNickname = showNickname
            if (users.isNotEmpty())
                binding.setNickname(users.find { user -> user.id == message.userId }?.nickname)
            else
                binding.setNickname("")
            binding.executePendingBindings()
        }
    }
}

@BindingAdapter("datetime")
fun adaptDateTimeSeparator(datetimeFrom: TextView, datetime: Long) {
    datetimeFrom.text = formatToYesterdayOrToday(Date(datetime))
}

@BindingAdapter("time")
fun adaptTime(timeFrom: TextView, datetime: Long) {
    timeFrom.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        ZonedDateTime.ofInstant(Date(datetime).toInstant(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    else
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(datetime))
}

@BindingAdapter("url")
fun loadImage(imageView: CircleImageView, nickname: String) {
    Glide.with(imageView)
        .load("https://picsum.photos/" + (nickname.hashCode().absoluteValue % 100 + 100))
        .into(imageView)
}

@BindingAdapter("withMargin", "showDateTime", "showNickname")
fun adaptMargins(
    message_block_from: LinearLayout,
    withMargin: Boolean,
    showDateTime: Boolean,
    showNickname: Boolean
) {
    val scale = message_block_from.context.resources.displayMetrics.density
    if ((withMargin || showNickname) && !showDateTime)
        if (showNickname)
            message_block_from.layoutParams =
                (message_block_from.layoutParams as ViewGroup.MarginLayoutParams)
                    .apply { setMargins(0, (20.0 * scale + 0.5).toInt(), 0, 0) }
        else
            message_block_from.layoutParams =
                (message_block_from.layoutParams as ViewGroup.MarginLayoutParams)
                    .apply { setMargins(0, (12.0 * scale + 0.5).toInt(), 0, 0) }
    else
        message_block_from.layoutParams =
            (message_block_from.layoutParams as ViewGroup.MarginLayoutParams)
                .apply { setMargins(0, (4.0 * scale + 0.5).toInt(), 0, 0) }
}

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
