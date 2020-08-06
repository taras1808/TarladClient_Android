package com.tarlad.client.models.dto

import com.tarlad.client.models.db.User

data class ChatLists(
    val id: Long,
    val title: String?,
    val users: ArrayList<User>
)