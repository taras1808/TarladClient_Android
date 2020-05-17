package com.tarlad.client.models

import com.google.gson.annotations.SerializedName

data class MessageCreator(
    val token: Token?,
    @SerializedName("chat_id")
    val chatId: Long?,
    val type: String?,
    val data: String?,
    val time: Long?
)