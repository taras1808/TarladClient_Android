package com.tarlad.client.ui.views.main.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tarlad.client.R
import com.tarlad.client.databinding.FragmentProfileBinding
import com.tarlad.client.ui.views.main.MainViewModel
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import java.io.ByteArrayOutputStream
import java.io.InputStream


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
        initImagePicker(container)
        vm.loadProfile()
        return binding.root
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
                R.layout.bottom_sheet_layout,
                container,
                false
            )
            bottomSheetDialog.setContentView(bottomSheetView)

            bottomSheetView.take.setOnTouchListener(touch)
            bottomSheetView.select.setOnTouchListener(touch)
            bottomSheetView.delete.setOnTouchListener(touch)
            bottomSheetView.cancel.setOnTouchListener(touch)

            bottomSheetView.take.setOnClickListener {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 437)
                bottomSheetDialog.dismiss()
            }
            bottomSheetView.select.setOnClickListener {
//                startActivity(Intent(context, ImagePickerActivity::class.java))
                val intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(intent, 438)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 438 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val uri = data.data!!
            vm.sendImage(encodeImage(uri))

            val selectedImage: Uri = data.data!!
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor = requireActivity().contentResolver.query(
                selectedImage,
                filePathColumn, null, null, null
            )!!
            cursor.moveToFirst()
            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
            val picturePath: String = cursor.getString(columnIndex)
            cursor.close()

        }

        if (requestCode == 437 && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val uri = data.extras?.get("data")
            val s = uri as Bitmap
        }
    }

    private fun encodeImage(path: Uri): String {
        val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(path)
        val bm = BitmapFactory.decodeStream(inputStream)
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
}
