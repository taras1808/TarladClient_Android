package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.db.User

@Dao
interface ChatListDao {

    @Query("SELECT user.* FROM chat JOIN chatList ON chat.id = chatList.chat_id JOIN user ON user.id = chatList.user_id WHERE chat.id == :chatId")
    fun getUsersByChatId(chatId: Long): List<User>

    fun insert(chatId: Long, users: List<Long>) = users.forEach { id -> insert(ChatList(chatId, id)) }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chatList: ChatList)

    @Delete
    fun delete(chatList: ChatList)

}
