package com.tarlad.client.ui.views.launcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tarlad.client.AppSession
import com.tarlad.client.ui.views.auth.AuthActivity
import com.tarlad.client.ui.views.main.MainActivity
import org.koin.android.ext.android.inject

class LauncherActivity : AppCompatActivity() {

    val appSession: AppSession by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = if (appSession.token == null) {
            Intent(this , AuthActivity::class.java)
        } else {
            Intent(this , MainActivity::class.java)
        }
        startActivity(intent)
        finish()
        super.onCreate(savedInstanceState)
    }
}
