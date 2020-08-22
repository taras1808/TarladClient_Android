package com.tarlad.client.models.db

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["chat_id", "user_id"])
data class ChatList(
    @ColumnInfo(name = "chat_id")
    val chatId: Long,
    @ColumnInfo(name = "user_id")
    val userId: Long
)
