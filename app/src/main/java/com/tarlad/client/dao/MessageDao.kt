package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.Message
import io.reactivex.rxjava3.core.Single

@Dao
interface MessageDao {

    @Query("SELECT * FROM Message WHERE chat_id=:chatId ORDER BY time DESC LIMIT 1")
    fun getLastMessageForChat(chatId: Long): Message?

    @Query("SELECT * FROM Message WHERE chat_id=:chatId ORDER BY time DESC LIMIT 10")
    fun getMessagesForChat(chatId: Long): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: Message)

    @Delete
    fun delete(message: Message?)
}