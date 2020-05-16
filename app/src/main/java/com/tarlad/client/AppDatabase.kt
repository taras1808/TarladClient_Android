package com.tarlad.client

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.dao.UserDao
import com.tarlad.client.models.Token
import com.tarlad.client.models.User

@Database(entities = [User::class, Token::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tokenDao(): TokenDao
}