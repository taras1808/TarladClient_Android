package com.tarlad.client.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "message")
data class Message(
    @PrimaryKey
    var id: String,
    @SerializedName(value = "chat_id")
    @ColumnInfo(name = "chat_id")
    var chatId: Long,
    @SerializedName(value = "user_id")
    @ColumnInfo(name = "user_id")
    var userId: Long,
    var type: String,
    var data: String,
    var time: Long
)