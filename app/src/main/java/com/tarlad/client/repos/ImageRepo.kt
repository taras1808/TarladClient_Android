package com.tarlad.client.repos

import io.reactivex.rxjava3.core.Single

interface ImageRepo {
    fun saveImage(ext: String, data: String)
    fun saveImageMessage(ext: String, data: String): Single<String>
    fun removeImage()
}