package com.tarlad.client.api

import com.tarlad.client.models.dto.ImageDTO
import io.reactivex.rxjava3.core.Single
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface ImagesApi {

    @Multipart
    @POST("api/images/upload")
    fun save(@Part image: MultipartBody.Part, ): Single<ImageDTO>
}
