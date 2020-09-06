package com.tarlad.client.ui.views.main.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tarlad.client.R
import com.tarlad.client.databinding.FragmentProfileBinding
import com.tarlad.client.enums.ImagePicker
import com.tarlad.client.helpers.createImageFile
import com.tarlad.client.helpers.encodeImage
import com.tarlad.client.helpers.getFileExtension
import com.tarlad.client.ui.views.main.MainViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.sheet_layout.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class ProfileFragment : Fragment() {

    val vm: MainViewModel by sharedViewModel()

    lateinit var binding: FragmentProfileBinding

    val disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this
        initImagePicker(container)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        disposable.add(vm.loadProfile())
    }

    override fun onDetach() {
        disposable.clear()
        super.onDetach()
    }

    private val touch = { v: View, event: MotionEvent ->
        if (event.action == MotionEvent.ACTION_DOWN)
            v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.alpha))
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initImagePicker(container: ViewGroup?) {
        binding.imageView.setOnTouchListener(touch)
        binding.imageView.setOnLongClickListener {

            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = layoutInflater.inflate(
                R.layout.sheet_layout,
                container,
                false
            )
            bottomSheetDialog.setContentView(bottomSheetView)

            bottomSheetView.take.setOnTouchListener(touch)
            bottomSheetView.select.setOnTouchListener(touch)
            bottomSheetView.delete.setOnTouchListener(touch)
            bottomSheetView.cancel.setOnTouchListener(touch)

            bottomSheetView.take.setOnClickListener {
                if (checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
                    requestPermissions(arrayOf(Manifest.permission.CAMERA).toList().toTypedArray(), 401)
                else
                    dispatchTakePictureIntent()
                bottomSheetDialog.dismiss()
            }
            bottomSheetView.select.setOnClickListener {
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ).apply { type = "image/*" }
                startActivityForResult(intent, ImagePicker.GALLERY)
                bottomSheetDialog.dismiss()
            }
            bottomSheetView.delete.setOnClickListener {
                vm.removeImage()
                bottomSheetDialog.dismiss()
            }
            bottomSheetView.cancel.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            val bottomSheetDialogFrameLayout =
                bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetDialogFrameLayout?.background = null

            bottomSheetDialog.show()

            true
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 401) {
            when {
                grantResults.isEmpty() -> { }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    dispatchTakePictureIntent()
                }
                grantResults[0] == PackageManager.PERMISSION_DENIED -> { }
            }
        }
    }

    private lateinit var currentPhotoPath: String

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile(requireActivity()).apply { currentPhotoPath = absolutePath }
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.tarlad.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, ImagePicker.TAKE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                ImagePicker.GALLERY -> {
                    if (data == null) return
                    val selectedImage: Uri = data.data!!
                    requireActivity().contentResolver.let {
                        val ext = getFileExtension(selectedImage, it)
                        vm.sendImage(ext , encodeImage(selectedImage, ext, it))
                    }
                }
                ImagePicker.TAKE -> {
                    BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->
                        vm.sendImage("jpg", encodeImage(bitmap))
                        File(currentPhotoPath).delete()
                    }
                }
            }
        }
    }
}
