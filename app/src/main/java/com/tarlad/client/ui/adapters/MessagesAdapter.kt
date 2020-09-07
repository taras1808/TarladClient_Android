package com.tarlad.client.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.databinding.ItemMessageFromMeBinding
import com.tarlad.client.databinding.ItemMessageFromMeMediaBinding
import com.tarlad.client.databinding.ItemMessageToMeBinding
import com.tarlad.client.databinding.ItemMessageToMeMediaBinding
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class MessagesAdapter(
    val messages: ArrayList<Message>,
    val users: ArrayList<User>,
    var userId: Long,
    var deleteListener: ((message: Message) -> Unit),
    var editListener: ((message: Message) -> Unit),
    var clickImageListener: ((url: String) -> Unit)
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    enum class MessagesAdapter {
        FROM, TO, FROM_MEDIA, TO_MEDIA
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MessagesAdapter.FROM.ordinal ->
                FromViewHolder(
                    ItemMessageFromMeBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    deleteListener,
                    editListener
                )
            MessagesAdapter.TO.ordinal ->
                ToViewHolder(
                    ItemMessageToMeBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    users
                )
            MessagesAdapter.FROM_MEDIA.ordinal ->
                FromMediaViewHolder(
                    ItemMessageFromMeMediaBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    deleteListener,
                    editListener,
                    clickImageListener
                )
            MessagesAdapter.TO_MEDIA.ordinal ->
                ToMediaViewHolder(
                    ItemMessageToMeMediaBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    ),
                    users,
                    clickImageListener
                )
            else -> throw Exception()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages.toList()[position]
        return if (message.userId == userId)
            if (message.type == "media")
                MessagesAdapter.FROM_MEDIA.ordinal
            else
                MessagesAdapter.FROM.ordinal
        else
            if (message.type == "media")
                MessagesAdapter.TO_MEDIA.ordinal
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
        messages.forEach {

            if (!this.messages.contains(it)) {

                this.messages.add(it)
                this.messages.sortByDescending { e -> e.time }

                val pos = this.messages.indexOf(it)
                if (pos == -1) return

                notifyItemInserted(pos)

                if (pos > 0)
                    notifyItemChanged(pos - 1)

                if (pos + 1 < this.messages.size - 1)
                    notifyItemChanged(pos + 1)
            }
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
            val pos =
                if (m.id == -1L)
                    this.messages.indexOf(m)
                else
                    this.messages.indexOfFirst { e -> e.id == m.id }
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

        messages.forEach {

            val count = this.messages.count { e -> e.id == it.id }

            if (count == 0) {
                this.messages.add(it)
                this.messages.sortByDescending { e -> e.time }
                val pos = this.messages.indexOf(it)
                notifyItemInserted(pos)
                if (pos + 1 < this.messages.size - 1)
                    notifyItemChanged(pos + 1)
                if (pos > 0)
                    notifyItemChanged(pos - 1)
            } else {
                if (!this.messages.contains(it)) {
                    val pos = this.messages.indexOfFirst { e -> e.id == it.id }
                    this.messages[pos] = it
                    notifyItemChanged(pos)
                }
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

    class FromViewHolder(
        private val binding: ItemMessageFromMeBinding,
        private val deleteListener: ((message: Message) -> Unit)?,
        private val editListener: ((message: Message) -> Unit)?
    ) : ViewHolder(binding.root) {

        override fun bind(
            message: Message,
            showImage: Boolean,
            showDateTime: Boolean,
            withMargin: Boolean,
            showNickname: Boolean
        ) {
            if (message.id > 0)
                binding.messageBlock.setOnCreateContextMenuListener { menu, _, _ ->
                    MenuInflater(binding.root.context).inflate(R.menu.context_menu_messages_from, menu)
                    menu.findItem(R.id.action_delete_message).setOnMenuItemClickListener {
                        deleteListener?.let { it(message) }
                        true
                    }
                    menu.findItem(R.id.action_edit_message).setOnMenuItemClickListener {
                        editListener?.let { it(message) }
                        true
                    }
                    menu.findItem(R.id.action_copy_message).setOnMenuItemClickListener {
                        val clipboard = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("", message.data)
                        clipboard.setPrimaryClip(clip)
                        true
                    }
                }
            else
                binding.messageBlock.setOnCreateContextMenuListener { menu, _, _ -> menu.clear() }
            binding.message = message
            binding.showDateTime = showDateTime
            binding.withMargin = withMargin
            binding.showNickname = showNickname
            binding.executePendingBindings()
        }
    }

    class ToViewHolder(
        private val binding: ItemMessageToMeBinding,
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
            binding.imageUrl = users.find { e -> e.id == message.userId }?.imageURL
            if (users.isNotEmpty())
                binding.setNickname(users.find { user -> user.id == message.userId }?.nickname)
            else
                binding.setNickname("")

            binding.messageBlockTo.setOnCreateContextMenuListener { menu, _, _ ->
                MenuInflater(binding.root.context).inflate(R.menu.context_menu_messages_to, menu)
                menu.findItem(R.id.action_copy_message).setOnMenuItemClickListener {
                    val clipboard = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("", message.data)
                    clipboard.setPrimaryClip(clip)
                    true
                }
            }

            binding.executePendingBindings()
        }
    }

    class FromMediaViewHolder(
        private val binding: ItemMessageFromMeMediaBinding,
        private val deleteListener: ((message: Message) -> Unit)?,
        private val editListener: ((message: Message) -> Unit)?,
        var clickImageListener: ((url: String) -> Unit)? = {},
    ) : ViewHolder(binding.root) {

        override fun bind(
            message: Message,
            showImage: Boolean,
            showDateTime: Boolean,
            withMargin: Boolean,
            showNickname: Boolean
        ) {
            if (message.id > 0)
                binding.icon.setOnCreateContextMenuListener { menu, _, _ ->
                    MenuInflater(binding.root.context).inflate(R.menu.context_menu_messages_from, menu)
                    menu.findItem(R.id.action_delete_message).setOnMenuItemClickListener {
                        deleteListener?.let { it(message) }
                        true
                    }
                }
            else
                binding.root.setOnCreateContextMenuListener { menu, _, _ -> menu.clear() }

            binding.icon.setOnClickListener {
                clickImageListener?.let { it(message.data) }
            }

            binding.message = message
            binding.showDateTime = showDateTime
            binding.withMargin = withMargin
            binding.showNickname = showNickname
            binding.executePendingBindings()
        }
    }

    class ToMediaViewHolder(
        private val binding: ItemMessageToMeMediaBinding,
        val users: List<User>,
        var clickImageListener: ((url: String) -> Unit)? = {}
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
            binding.imageUrl = users.find { e -> e.id == message.userId }?.imageURL
            if (users.isNotEmpty())
                binding.nickname = users.find { user -> user.id == message.userId }?.nickname
            else
                binding.nickname = ""

            binding.icon.setOnClickListener {
                clickImageListener?.let { it(message.data) }
            }

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
fun loadImage(imageView: CircleImageView, url: String?) {
        Glide.with(imageView)
            .load(url)
            .placeholder(R.drawable.ic_baseline_person_24)
            .error(R.drawable.ic_baseline_person_24)
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
