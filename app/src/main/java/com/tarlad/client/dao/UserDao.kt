package com.tarlad.client.dao

import androidx.room.*
import com.tarlad.client.models.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Insert
    fun insertAll(vararg users: User)

    @Insert
    fun insertAll(users: List<User>)

    @Insert
    fun insertAll(users: Set<User>)

    @Delete
    fun delete(user: User)

    @Delete
    fun deleteAll(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(users: List<User>)
}