package com.tarlad.client.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Token(
    @PrimaryKey
    val value: String,
    @ColumnInfo(name = "user_id")
    @SerializedName(value = "user_id")
    val userId: Long,
    val time: Long
)