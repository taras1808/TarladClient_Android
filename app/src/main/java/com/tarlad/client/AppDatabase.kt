package com.tarlad.client

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tarlad.client.dao.*
import com.tarlad.client.models.db.*

@Database(entities = [User::class, RefreshToken::class, Chat::class, Message::class, ChatList::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun tokenDao(): TokenDao
    abstract fun chatDao(): ChatDao
    abstract fun chatListDao(): ChatListDao
    abstract fun messagesDao(): MessageDao
}
