package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.Token

@Dao
interface TokenDao {

    @Query("SELECT * FROM token LIMIT 1")
    fun getToken(): Token?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(token: Token)

    @Delete
    fun delete(token: Token?)
}