package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.Chat

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat WHERE id == :id")
    fun getChatById(id: Long): Chat?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chat: Chat)

    @Delete
    fun delete(chat: Chat?)
}