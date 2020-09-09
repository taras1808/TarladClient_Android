package com.tarlad.client.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.tarlad.client.R
import com.tarlad.client.databinding.ItemMessageFromMeBinding
import com.tarlad.client.databinding.ItemMessageFromMeMediaBinding
import com.tarlad.client.databinding.ItemMessageToMeBinding
import com.tarlad.client.databinding.ItemMessageToMeMediaBinding
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import java.lang.Exception
import java.util.*

// TODO change listeners to action
class MessagesAdapter(
    val messages: ArrayList<Message>,
    val users: ArrayList<User>,
    var userId: Long,
    private var deleteListener: ((message: Message) -> Unit),
    private var editListener: ((message: Message) -> Unit),
    private var clickImageListener: ((url: String) -> Unit)
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
        private var clickImageListener: ((url: String) -> Unit)? = {},
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
                    menu.findItem(R.id.action_edit_message).isVisible = false
                    menu.findItem(R.id.action_copy_message).isVisible = false
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