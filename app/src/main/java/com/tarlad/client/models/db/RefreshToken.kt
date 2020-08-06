package com.tarlad.client.models.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RefreshToken(
    @PrimaryKey
    val value: String,
    val userId: Long
)