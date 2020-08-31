package com.tarlad.client.models.dto

import com.google.gson.annotations.SerializedName
import com.tarlad.client.models.db.User

data class ChatLists(
    val id: Long,
    val title: String?,
    @SerializedName("user_id")
    val userId: Long,
    val users: ArrayList<User>
)
