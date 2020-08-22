package com.tarlad.client.ui.views.main.fragments

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.dao.UserDao
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.ui.views.main.MainViewModel
import io.reactivex.rxjava3.core.Single
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.android.ext.android.inject
import kotlin.math.absoluteValue

class ProfileFragment: Fragment(R.layout.fragment_profile) {

    val vm: MainViewModel by activityViewModels()
    private val userDao: UserDao by inject()


    @SuppressLint("SetTextI18n")
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
