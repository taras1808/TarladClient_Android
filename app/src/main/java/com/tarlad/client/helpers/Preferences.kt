package com.tarlad.client.helpers

import android.content.Context
import androidx.annotation.StringRes
import com.tarlad.client.R

class Preferences(private val context: Context) {

    private val sp = context.getSharedPreferences("tarlad", Context.MODE_PRIVATE)



    fun getString(@StringRes res: Int): String {
        return sp.getString(context.getString(res), "") ?: ""
    }

    fun getInt(@StringRes res: Int, default: Int): Int {
        return sp.getInt(context.getString(res), default)
    }

    fun set(@StringRes res: Int, value: String) {
        sp.edit().putString(context.getString(res), value).apply()
    }

    fun set(@StringRes res: Int, value: Int) {
        sp.edit().putInt(context.getString(res), value).apply()
    }
}