package com.tarlad.client.ui.views.main.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.dao.UserDao
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.ui.views.main.MainViewModel
import io.reactivex.rxjava3.core.Single
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.android.ext.android.inject
import kotlin.math.absoluteValue

class ProfileFragment(val vm: MainViewModel): Fragment(R.layout.fragment_profile) {

    val userDao: UserDao by inject()


    override fun onAttach(context: Context) {
        super.onAttach(context)

        Single.fromCallable { userDao.getById(vm.appSession.userId ?: -1) }
            .ioMain()
            .subscribe({
                full_name.text = "${it?.name} ${it?.surname}"

                Glide.with(requireContext())
                    .load("https://picsum.photos/" + (it?.nickname.hashCode().absoluteValue % 100 + 100))
                    .into(imageView)

                vm.title.value = it?.nickname

            }, { it.printStackTrace() })
    }
}