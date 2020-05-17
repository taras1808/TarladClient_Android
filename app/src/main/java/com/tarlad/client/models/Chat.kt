package com.tarlad.client.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Chat(
    @PrimaryKey
    val id: Long?,
    val title: String?
)
