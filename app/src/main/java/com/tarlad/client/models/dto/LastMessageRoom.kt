package com.tarlad.client.models.dto

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.tarlad.client.models.db.Message

data class LastMessageRoom(
    val chatId: Long,
    val title: String?,
    var id: Long,
    var userId: Long,
    var type: String,
    var data: String,
    var time: Long
)