package com.tarlad.client.models.dto

import com.tarlad.client.models.db.RefreshToken

data class Token(
    val token: String,
    val refreshToken: RefreshToken
)