package com.tarlad.client.ui.views.image

import android.Manifest
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.AttributeSet
import android.widget.GridView
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.tarlad.client.R
import kotlinx.android.synthetic.main.activity_image_picker.*
import java.lang.Exception

class ImagePickerActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)
        requestPermissions(arrayListOf(Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(), 3)
        getImagesPath().forEach {
            try {
                val image = ImageView(applicationContext)
                Glide.with(applicationContext).load(it)
                    .into(image)
                root.addView(image)
            } catch (e: Exception) {
            }
        }

    }
    fun getImagesPath(): ArrayList<String> {
        val uri: Uri
        val listOfAllImages = ArrayList<String>()
        val cursor: Cursor
        val column_index_data: Int
        val column_index_folder_name: Int
        var PathOfImage: String? = null
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        cursor = contentResolver.query(
            uri, projection, null,
            null, null
        )!!
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        while (cursor.moveToNext()) {
            PathOfImage = cursor.getString(column_index_data)
            listOfAllImages.add(PathOfImage)
        }
        return listOfAllImages
    }
}