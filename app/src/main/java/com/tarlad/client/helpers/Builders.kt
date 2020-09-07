package com.tarlad.client.helpers

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import com.tarlad.client.models.db.User
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.DateFormat
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

fun encodeImage(path: Uri, mime: String, contentResolver: ContentResolver): ByteArray {
    val inputStream: InputStream? = contentResolver.openInputStream(path)
    val bm = BitmapFactory.decodeStream(inputStream)
    val baos = ByteArrayOutputStream()
    when (mime) {
        "jpg" -> bm.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        "png" -> bm.compress(Bitmap.CompressFormat.PNG, 30, baos)
        "webp" -> bm.compress(Bitmap.CompressFormat.WEBP, 30, baos)
        else -> bm.compress(Bitmap.CompressFormat.JPEG, 30, baos)
    }
    return baos.toByteArray()
}

fun encodeImage(bm: Bitmap): ByteArray {
    val baos = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.JPEG, 30, baos)
    return baos.toByteArray()
}

fun formatToYesterdayOrToday(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DATE, -1)
    val lastWeek = Calendar.getInstance()
    lastWeek.add(Calendar.DATE, -7)
    val timeFormatter: DateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    return if (calendar[Calendar.YEAR] == today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]
    ) {
        "Today " + timeFormatter.format(date)
    } else if (calendar[Calendar.YEAR] == yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR]
    ) {
        "Yesterday " + timeFormatter.format(date)
    } else {
        if (calendar[Calendar.DAY_OF_YEAR] > lastWeek[Calendar.DAY_OF_YEAR])
            SimpleDateFormat("EEEE HH:mm", Locale.getDefault()).format(date)
        else
            SimpleDateFormat("dd MMMM YYYY HH:mm", Locale.getDefault()).format(date)
    }
}