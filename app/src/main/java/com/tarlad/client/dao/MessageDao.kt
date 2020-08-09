package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.dto.LastMessageRoom
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.dto.LastMessage
import io.reactivex.Observable

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE id = :id")
    fun getById(id: String): Message?

    @Query("SELECT message.chat_id FROM message JOIN chat ON message.chat_id = chat.id")
    fun getAll(): List<Long>

    @Query("SELECT * FROM message GROUP BY chat_id ORDER BY message.time DESC")
    fun getLastMessage(): Observable<List<Message>>

    @Query("SELECT * FROM message WHERE chat_id = :chatId ORDER BY time DESC LIMIT 1")
    fun getLastMessageForChat(chatId: Long): Message?

    @Query("SELECT * FROM message WHERE chat_id = :chatId AND time < :time ORDER BY time DESC LIMIT 10 OFFSET (10 * :page)")
    fun getMessagesForChatBeforeTime(chatId: Long, time: Long, page: Long): List<Message>

    @Query("SELECT * FROM message WHERE chat_id = :chatId AND time > :time ORDER BY time ASC LIMIT 5 OFFSET (5 * :page)")
    fun getMessagesForChatAfterTimeObservable(chatId: Long, time: Long, page: Long): Observable<List<Message>>

    fun getDistinctMessagesForChatAfterTimeObservable(chatId: Long, time: Long, page: Long): Observable<List<Message>>
        = getMessagesForChatAfterTimeObservable(chatId, time, page).distinctUntilChanged()

    @Query("SELECT * FROM message WHERE chat_id = :chatId AND time < :time ORDER BY time DESC LIMIT 10 OFFSET (10 * :page)")
    fun getMessagesForChatObservable(chatId: Long, time: Long, page: Long): Observable<List<Message>>

    fun getDistinctMessagesForChatObservable(chatId: Long, time: Long, page: Long): Observable<List<Message>>
            = getMessagesForChatObservable(chatId, time, page).distinctUntilChanged()

    @Query("SELECT * FROM message WHERE chat_id = :chatId AND time <= :timeTo AND time > :timeFrom ORDER BY time ASC")
    fun getNewMessagesForChatObservable(chatId: Long, timeTo: Long, timeFrom: Long): Observable<List<Message>>

    fun getDistinctNewMessagesForChatObservable(chatId: Long, timeTo: Long, timeFrom: Long): Observable<List<Message>>
            = getNewMessagesForChatObservable(chatId, timeTo, timeFrom).distinctUntilChanged()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg messages: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: List<Message>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: Set<Message>)

    @Delete
    fun delete(message: Message)

    @Delete
    fun deleteAll(messages: List<Message>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(messages: List<Message>)
}