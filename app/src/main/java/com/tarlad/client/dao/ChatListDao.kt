package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.db.ChatList
import com.tarlad.client.models.db.User

@Dao
interface ChatListDao {

    @Query("SELECT user.id FROM chat JOIN chatList ON chat.id = chatList.chat_id JOIN user ON user.id = chatList.user_id WHERE chat.id == :chatId AND user.id != :userId")
    fun getUsersIdByChatId(chatId: Long, userId: Long): List<Long>

    @Query("SELECT chatList.user_id FROM chat JOIN chatList WHERE chatList.chat_id == :chatId")
    fun getUsersIdByChatId(chatId: Long): List<Long>

    @Query("SELECT user.nickname FROM chat JOIN chatList ON chat.id = chatList.chat_id JOIN user ON user.id = chatList.user_id WHERE chat.id == :chatId AND user.id != :userId")
    fun getUsersNicknameByChatId(chatId: Long, userId: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chatList: ChatList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg chatLists: ChatList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: Set<User>)

    @Delete
    fun delete(chatList: ChatList)

    @Delete
    fun deleteAll(chatLists: List<ChatList>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(chatLists: List<ChatList>)
}