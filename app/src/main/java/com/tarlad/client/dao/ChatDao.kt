package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.db.Chat
import io.reactivex.Observable

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat WHERE id == :id")
    fun getById(id: Long): Chat?

    @Query("SELECT chat.user_id FROM chat WHERE id == :id")
    fun getAdmin(id: Long): Long

    @Query("SELECT chat.title FROM chat WHERE id == :id")
    fun getTitle(id: Long): String?

    @Query("SELECT * FROM chat WHERE id == :id")
    fun observe(id: Long): Observable<Chat>

    fun observeDistinct(id: Long): Observable<Chat> = observe(id).distinctUntilChanged()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(chat: Chat)

    @Delete
    fun delete(chat: Chat?)
}
