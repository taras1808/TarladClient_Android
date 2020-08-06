package com.tarlad.client.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User
import kotlinx.android.synthetic.main.message_frame.view.*
import kotlinx.android.synthetic.main.message_from_me.view.*
import kotlinx.android.synthetic.main.message_to_me.view.*
import kotlinx.android.synthetic.main.message_to_me_textedit.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.absoluteValue

class MessagesAdapter(
    val pairs: HashMap<String, List<Message>> = hashMapOf(),
    val messages: SortedSet<Message> = sortedSetOf(Comparator { o1, o2 -> o2.time.compareTo(o1.time) }),
    val users: ArrayList<User> = arrayListOf(),
    var userId: Long = -1,
    var listener: ((time: Long) -> Unit)? = {}
) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>(){

    var array: ArrayList<ArrayList<Message>> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_frame, parent, false)
        return ViewHolder(
            view,
            users,
            userId
        )
    }

    override fun getItemCount(): Int {
        var tmp: ArrayList<Message> = ArrayList()
        var time: Long = 0
        var author = ""
        array.clear()
        messages.forEach { message ->
            if (author != message.userId.toString() || time - message.time > 60_000){
                tmp = ArrayList<Message>().apply { add(message) }
                array.add(tmp)
            }else{
                tmp.add(message)
            }
            time = message.time
            author = message.userId.toString()
        }
        return array.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = array[position]
        if (position == array.size - 1) listener?.let {
            it(message.minBy { e -> e.time }?.time ?: 0)
        }
        holder.showNickname = position == array.size - 1 || array[position + 1].first().userId != message.first().userId
        holder.messages = message
    }

    class ViewHolder(val view: View, val users: List<User>, val userId: Long): RecyclerView.ViewHolder(view){

        var showNickname: Boolean = true

        var messages: ArrayList<Message> = ArrayList()
            set(value) {

                field = value

                view.message_frame.removeAllViews()

                if (messages.isEmpty()) return

                if (messages.first().userId != userId){

                    val messagesToMeList = LayoutInflater.from(view.context).inflate(R.layout.message_to_me, view.message_frame)

                    if (showNickname){
                        messagesToMeList.nickname.text = users.find { user -> user.id == messages.first().userId }?.nickname
                    }else{
                        messagesToMeList.nickname.visibility = View.GONE
                    }

                    value.reversed().forEach {
                        val txt = LayoutInflater.from(view.context).inflate(R.layout.message_to_me_textedit, messagesToMeList.messages_to_me, false)
                        txt.message.text = it.data
                        messagesToMeList.messages_to_me.addView(txt)
                    }


                    Glide.with(messagesToMeList)
                        .load("https://picsum.photos/" + (users.find { it.id == value.first().userId }?.nickname.hashCode().absoluteValue % 100 + 100))
                        .into(messagesToMeList.imageView)

                } else {

                    val messagesFromMeList = LayoutInflater.from(view.context).inflate(R.layout.message_from_me, view.message_frame)

                    value.reversed().forEach {
                        val txt = LayoutInflater.from(view.context).inflate(R.layout.message_from_me_textedit, messagesFromMeList.messages_from_me, false)
                        txt.message.text = it.data
                        messagesFromMeList.messages_from_me.addView(txt)
                    }

                }
            }
    }
}