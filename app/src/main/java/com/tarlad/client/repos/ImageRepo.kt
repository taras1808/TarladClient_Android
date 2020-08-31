package com.tarlad.client.repos

interface ImageRepo {
    fun saveImage(data: String)
    fun removeImage()
}