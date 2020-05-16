package com.tarlad.client.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey
    var id: Int?,
    var email: String = "",
    var password: String = "",
    var nickname: String = "",
    var name: String = "",
    var surname: String = "",
    @ColumnInfo(name = "image_url")
    var imageURL: String? = null
)