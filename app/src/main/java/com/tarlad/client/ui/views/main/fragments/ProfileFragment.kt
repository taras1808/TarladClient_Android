package com.tarlad.client.ui.views.main.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.tarlad.client.R
import com.tarlad.client.dao.UserDao
import com.tarlad.client.databinding.FragmentLoginBinding
import com.tarlad.client.databinding.FragmentProfileBinding
import com.tarlad.client.helpers.ioMain
import com.tarlad.client.ui.views.main.MainViewModel
import io.reactivex.rxjava3.core.Single
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.android.ext.android.inject
import kotlin.math.absoluteValue

class ProfileFragment: Fragment() {

    val vm: MainViewModel by activityViewModels()

    lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onAttach(context: Context) {
        super.onAttach(context)

        vm.loadProfile()
    }
}
