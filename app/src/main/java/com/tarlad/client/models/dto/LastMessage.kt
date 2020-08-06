package com.tarlad.client.models.dto

import com.tarlad.client.models.db.Chat
import com.tarlad.client.models.db.Message

data class LastMessage(
    val id: Long,
    val title: String?,
    val message: Message
)