package com.tarlad.client.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey
    var idUser: Int = 0,
    var name: String = "",
    var surname: String = "",
    var email: String = "",
    var password: String = "",
    var avatar: String? = null
)