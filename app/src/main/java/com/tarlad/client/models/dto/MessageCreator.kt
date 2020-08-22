package com.tarlad.client.models.dto

import com.google.gson.annotations.SerializedName
import com.tarlad.client.models.db.RefreshToken

data class MessageCreator(
    val chatId: Long,
    val type: String,
    val data: String,
    val time: Long
)