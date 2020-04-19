package com.tarlad.client.helpers

import android.content.Context
import androidx.annotation.StringRes
import com.tarlad.client.R

class Preferences(private val context: Context) {

    private val sp = context.getSharedPreferences("tarlad", Context.MODE_PRIVATE)

    var token: String
        get() = getString(R.string.token_key)
        set(value) = set(R.string.token_key, value)

    var idUser: Int
        get() = getInt(R.string.id_user_key, -1)
        set(value) = set(R.string.id_user_key, value)



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