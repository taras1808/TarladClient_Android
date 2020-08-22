package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.db.Message

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE id = :id")
    fun getById(id: Long): Message?

    @Query("SELECT message.chat_id FROM message JOIN chat ON message.chat_id = chat.id")
    fun getAll(): List<Long>

    @Query("SELECT * FROM message WHERE time < :time GROUP BY chat_id ORDER BY message.time DESC LIMIT 10 OFFSET (10 * :page)")
    fun getLastMessagesBeforeTime(time: Long, page: Long): List<Message>

    @Query("SELECT * FROM message WHERE chat_id = :chatId ORDER BY time DESC LIMIT 1")
    fun getLastMessageForChat(chatId: Long): Message?

    @Query("SELECT * FROM message WHERE chat_id = :chatId AND time < :time ORDER BY time DESC LIMIT 10 OFFSET (10 * :page)")
    fun getMessagesForChatBeforeTime(chatId: Long, time: Long, page: Long): List<Message>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: List<Message>)

    @Delete
    fun delete(message: Message)

    @Delete
    fun deleteAll(messages: List<Message>)

    @Query("DELETE FROM message")
    fun truncate()
}
