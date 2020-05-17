package com.tarlad.client

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tarlad.client.dao.ChatDao
import com.tarlad.client.dao.MessageDao
import com.tarlad.client.dao.TokenDao
import com.tarlad.client.dao.UserDao
import com.tarlad.client.models.Chat
import com.tarlad.client.models.Message
import com.tarlad.client.models.Token
import com.tarlad.client.models.User

@Database(entities = [User::class, Token::class, Chat::class, Message::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tokenDao(): TokenDao
    abstract fun chatDao(): ChatDao
    abstract fun messagesDao(): MessageDao
}