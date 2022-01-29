package com.tarlad.client.helpers

import android.os.Build
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.tarlad.client.R
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class ImageMessage(val url: String, val width: Int, val height: Int)

fun loadImage(imageView: ImageView, url: String?) {
    if (url.isNullOrEmpty()) return
    val data = Gson().fromJson(url, ImageMessage::class.java)
    val ratio = data.width.toDouble() / data.height.toDouble()
    imageView.layoutParams.width = (150 * ratio * imageView.context.resources.displayMetrics.density).toInt()
    imageView.layoutParams.height = (150 * imageView.context.resources.displayMetrics.density).toInt()
    Glide.with(imageView)
        .asBitmap()
        .load(data.url)
        .transform(RoundedCorners((24 * imageView.context.resources.displayMetrics.density).toInt()))
        .override((150 * ratio * imageView.context.resources.displayMetrics.density).toInt(),(150 * imageView.context.resources.displayMetrics.density).toInt())
        .into(imageView)
}


fun loadImageNotCircle(imageView: ImageView, url: String?) {
    if (url.isNullOrEmpty()) return
    val data = Gson().fromJson(url, ImageMessage::class.java)
    Glide.with(imageView)
        .asBitmap()
        .load(data.url)
        .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
        .into(imageView)
}


fun adaptDateTimeSeparator(datetimeFrom: TextView, datetime: Long) {
    datetimeFrom.text = formatToYesterdayOrToday(Date(datetime))
}

fun adaptTime(timeFrom: TextView, datetime: Long) {
    timeFrom.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        ZonedDateTime.ofInstant(Date(datetime).toInstant(), ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    else
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(datetime))
}

fun loadImage(imageView: CircleImageView, url: String?) {
    Glide.with(imageView)
        .load(url)
        .placeholder(R.drawable.ic_baseline_person_24)
        .error(R.drawable.ic_baseline_person_24)
        .into(imageView)
}

fun adaptMargins(
    layout: LinearLayout,
    withMargin: Boolean,
    showDateTime: Boolean,
    showNickname: Boolean
) {
    val scale = layout.context.resources.displayMetrics.density
    if ((withMargin || showNickname) && !showDateTime)
        if (showNickname)
            layout.layoutParams =
                (layout.layoutParams as ViewGroup.MarginLayoutParams)
                    .apply { setMargins(0, (20.0 * scale + 0.5).toInt(), 0, 0) }
        else
            layout.layoutParams =
                (layout.layoutParams as ViewGroup.MarginLayoutParams)
                    .apply { setMargins(0, (12.0 * scale + 0.5).toInt(), 0, 0) }
    else
        layout.layoutParams =
            (layout.layoutParams as ViewGroup.MarginLayoutParams)
                .apply { setMargins(0, (4.0 * scale + 0.5).toInt(), 0, 0) }
}

fun EditText.bindText(lifecycleOwner: LifecycleOwner, liveData: MutableLiveData<String>) {
    liveData.observe(lifecycleOwner) {
        if (text.toString() != it.toString())
            setText(it)
    }

    addTextChangedListener {
        liveData.value = it.toString()
    }
}

