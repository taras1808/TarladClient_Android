package com.tarlad.client.models.db

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

@Entity
data class User(
    @PrimaryKey
    var id: Long,
    @Ignore
    var email: String? = null,
    @Ignore
    var password: String? = null,
    var nickname: String,
    var name: String,
    var surname: String,
    @ColumnInfo(name = "image_url")
    @SerializedName("image_url")
    var imageURL: String? = null
){
    constructor(
        id: Long,
        nickname: String,
        name: String,
        surname: String,
        imageURL: String?
    ) : this(id, null, null, nickname, name, surname, imageURL)
}
