package com.tarlad.client.models.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Chat(
    @PrimaryKey
    val id: Long,
    val title: String?,
    @SerializedName("user_id")
    @ColumnInfo(name = "user_id")
    val userId: Long
)
