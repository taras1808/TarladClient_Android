package com.tarlad.client.helpers

import android.content.Context
import androidx.annotation.StringRes
import com.tarlad.client.R

class Preferences(private val context: Context) {

    private val sp = context.getSharedPreferences("tarlad", Context.MODE_PRIVATE)

    var token: String?
        get() = getString(R.string.token_key)
        set(value) = set(R.string.token_key, value)

    var userId: Long?
        get() = getLong(R.string.user_key)
        set(value) = set(R.string.user_key, value)

    fun getString(@StringRes res: Int): String? {
        return sp.getString(context.getString(res), null)
    }

    fun getInt(@StringRes res: Int): Int {
        return sp.getInt(context.getString(res), -1)
    }

    fun getLong(@StringRes res: Int): Long {
        return sp.getLong(context.getString(res), -1)
    }

    fun set(@StringRes res: Int, value: String?) {
        sp.edit().putString(context.getString(res), value).apply()
    }

    fun set(@StringRes res: Int, value: Int?) {
        sp.edit().putInt(context.getString(res), value ?: -1).apply()
    }

    fun set(@StringRes res: Int, value: Long?) {
        sp.edit().putLong(context.getString(res), value ?: -1).apply()
    }
}
