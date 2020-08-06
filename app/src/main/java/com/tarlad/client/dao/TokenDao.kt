package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.db.RefreshToken
import io.reactivex.Single

@Dao
interface TokenDao {

    @Query("SELECT * FROM RefreshToken LIMIT 1")
    fun getToken(): RefreshToken?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(token: RefreshToken)

    @Delete
    fun delete(token: RefreshToken?)
}