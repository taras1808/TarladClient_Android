package com.tarlad.client.helpers

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson


@BindingAdapter("onNavigationItemSelected")
fun setOnNavigationItemSelectedListener(
    view: BottomNavigationView,
    listener: BottomNavigationView.OnNavigationItemSelectedListener?
) {
    view.setOnNavigationItemSelectedListener(listener)
}

data class ImageMessage(val url: String, val width: Int, val height: Int)

@BindingAdapter("url")
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


@BindingAdapter("urlNotCircle")
fun loadImageNotCircle(imageView: ImageView, url: String?) {

    if (url.isNullOrEmpty()) return

    val data = Gson().fromJson(url, ImageMessage::class.java)

    Glide.with(imageView)
        .asBitmap()
        .load(data.url)
        .override(SIZE_ORIGINAL, SIZE_ORIGINAL)
        .into(imageView)
}
