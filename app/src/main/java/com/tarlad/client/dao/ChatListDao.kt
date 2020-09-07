package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.db.User

@Dao
interface ChatListDao {

    @Query("SELECT user.* FROM chat JOIN chatList ON chat.id = chatList.chat_id JOIN user ON user.id = chatList.user_id WHERE chat.id == :chatId")
    fun getUsersByChatId(chatId: Long): List<User>

    @Query("SELECT user.id FROM chat JOIN chatList ON chat.id = chatList.chat_id JOIN user ON user.id = chatList.user_id WHERE chat.id == :chatId")
    fun getUsersIdsByChatId(chatId: Long): List<Long>

//    fun insert(chatId: Long, users: List<Long>) = users.forEach { id -> insert(ChatList(chatId, id)) }

    fun insert(chatId: Long, users: List<User>) = users.forEach { user -> insert(ChatList(chatId, user.id)) }

    fun delete(chatId: Long, users: List<User>) = users.forEach { user -> delete(ChatList(chatId, user.id)) }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chatList: ChatList)

    @Delete
    fun delete(chatList: ChatList)

}
