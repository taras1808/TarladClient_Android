package com.tarlad.client.ui.views.image

import android.Manifest
import android.annotation.SuppressLint
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.tarlad.client.databinding.ActivityImagePickerBinding
import java.lang.Exception

class ImagePickerActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityImagePickerBinding = ActivityImagePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermissions(arrayListOf(Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(), 3)
        getImagesPath().forEach {
            try {
                val image = ImageView(applicationContext)
                Glide.with(applicationContext).load(it)
                    .into(image)
                binding.root.addView(image)
            } catch (e: Exception) {
            }
        }

    }
    @SuppressLint("Recycle")
    fun getImagesPath(): ArrayList<String> {
        val listOfAllImages = ArrayList<String>()
        val cursor: Cursor
        var pathOfImage: String?
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        cursor = contentResolver.query(
            uri, projection, null,
            null, null
        )!!
        val columnIndexData: Int = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
//        val columnIndexFolderName: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        while (cursor.moveToNext()) {
            pathOfImage = cursor.getString(columnIndexData)
            listOfAllImages.add(pathOfImage)
        }
        return listOfAllImages
    }
}