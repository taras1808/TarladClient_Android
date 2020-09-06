package com.tarlad.client.helpers

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import com.tarlad.client.models.db.User
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

fun getTitle(title: String?, users: List<User>, userId: Long): String {
    return title ?: if (users.isEmpty()) ""
        else {
            val u = users.filter { e -> e.id != userId }
            if (u.isNotEmpty()) u.map { e -> e.nickname }.reduceRight { s, acc -> "$s, $acc" }
            else users.first().nickname
        }
}

@Throws(IOException::class)
fun createImageFile(activity: Activity): File {
    val timeStamp: String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? =
        activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}

fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String {
    val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
    return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ?: "jpg"
}

fun encodeImage(path: Uri, mime: String, contentResolver: ContentResolver): String {
    val inputStream: InputStream? = contentResolver.openInputStream(path)
    val bm = BitmapFactory.decodeStream(inputStream)
    val baos = ByteArrayOutputStream()
    when (mime) {
        "jpg" -> bm.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        "png" -> bm.compress(Bitmap.CompressFormat.PNG, 30, baos)
        "webp" -> bm.compress(Bitmap.CompressFormat.WEBP, 30, baos)
        else -> bm.compress(Bitmap.CompressFormat.JPEG, 30, baos)
    }
    val b: ByteArray = baos.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
}

fun encodeImage(bm: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.JPEG, 30, baos)
    val b: ByteArray = baos.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
}