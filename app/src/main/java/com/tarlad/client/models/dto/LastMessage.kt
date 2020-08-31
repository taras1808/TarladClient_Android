package com.tarlad.client.models.dto

import com.google.gson.annotations.SerializedName
import com.tarlad.client.models.db.Message
import com.tarlad.client.models.db.User

data class LastMessage(
    val id: Long,
    val title: String?,
    val userId: Long,
    val message: Message,
    val users: List<User>
)
