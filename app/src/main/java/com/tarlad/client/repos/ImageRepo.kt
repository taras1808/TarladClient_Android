package com.tarlad.client.repos

import com.tarlad.client.models.dto.ImageDTO
import io.reactivex.rxjava3.core.Single

interface ImageRepo {
    fun saveImage(ext: String, data: ByteArray)
    fun saveImageMessage(ext: String, data: ByteArray): Single<ImageDTO>
    fun removeImage()
}