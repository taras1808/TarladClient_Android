package com.tarlad.client.repos.impl

import android.os.AsyncTask
import com.google.gson.Gson
import com.tarlad.client.AppSession
import com.tarlad.client.api.ImagesApi
import com.tarlad.client.dao.UserDao
import com.tarlad.client.enums.Events
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.models.db.User
import com.tarlad.client.models.dto.ImageDTO
import com.tarlad.client.repos.ImageRepo
import io.reactivex.rxjava3.core.Single
import io.socket.client.Ack
import io.socket.client.Socket
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ImageRepoImpl(
    private val socket: Socket,
    private val appSession: AppSession,
    private val userDao: UserDao,
    private val imagesApi: ImagesApi
) : ImageRepo {

    override fun saveImage(ext: String, data: ByteArray) {
        val image = MultipartBody.Part.createFormData(
            "pic",
            appSession.userId!!.toString(),
            data.toRequestBody("image/*".toMediaTypeOrNull(), 0, data.size)
        )
        //TODO
        imagesApi.save(image)
            .ioMain()
            .subscribe(
                { imageDTO ->
                    socket.emit(Events.USERS_IMAGES, imageDTO.url, Ack { array ->
                        val user: User = Gson().fromJson(array[0].toString(), User::class.java)
                        userDao.insert(user)
                    })
                }, {}
            )
    }

    override fun saveImageMessage(ext: String, data: ByteArray): Single<ImageDTO> {
        val image = MultipartBody.Part.createFormData(
            "pic",
            appSession.userId!!.toString(),
            data.toRequestBody("image/*".toMediaTypeOrNull(), 0, data.size)
        )
        return imagesApi.save(image)
    }

    //TODO
    override fun removeImage() {
        AsyncTask.execute {
            userDao.getById(appSession.userId ?: return@execute)?.imageURL ?: return@execute
            socket.emit(Events.USERS_IMAGES_DELETE, Ack { array ->
                val user: User = Gson().fromJson(array[0].toString(), User::class.java)
                userDao.insert(user)
            })
        }
    }
}