package com.tarlad.client.models.dto


data class MessageCreator(
    val chatId: Long,
    val type: String,
    val data: String,
    val time: Long
)
