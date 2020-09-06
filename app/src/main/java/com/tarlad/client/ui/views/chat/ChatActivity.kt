package com.tarlad.client.ui.views.chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityChatBinding
import com.tarlad.client.enums.ImagePicker
import com.tarlad.client.enums.Messages
import com.tarlad.client.helpers.createImageFile
import com.tarlad.client.helpers.encodeImage
import com.tarlad.client.helpers.getFileExtension
import com.tarlad.client.ui.animators.MessageItemAnimator
import com.tarlad.client.ui.adapters.MessagesAdapter
import com.tarlad.client.ui.views.chat.details.ChatDetailsActivity
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.sheet_chat.view.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sqrt


class ChatActivity : AppCompatActivity() {

    private val vm by viewModel<ChatViewModel> { parametersOf(lifecycleScope.id) }
    private lateinit var adapter: MessagesAdapter
    private var chatId: Long = -1
    private var title: String = ""

    lateinit var binding: ActivityChatBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = intent.getStringExtra("TITLE") ?: ""
        chatId = intent.getLongExtra("ID", -1L)
        vm.title.value = title



        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        binding.vm = vm
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP

        initRecyclerView()
        initAdapter()


        observeError()
        observeUsers()
        observeMessages()


        var x = 0F
        var y = 0F
        var distance = 0F
        var action = 0
        binding.d.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.x
                    y = event.y
                    binding.image.translationX = 0F
                    binding.image.translationY = 0F
                    distance = 0F
                    action = 0


                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    var xCord = 0F
                    var yCord = 0F

                    repeat(event.pointerCount) {
                        xCord += event.getX(it)
                        yCord += event.getY(it)
                    }

                    x = xCord / event.pointerCount - binding.image.translationX
                    y = yCord / event.pointerCount - binding.image.translationY
                }
                MotionEvent.ACTION_MOVE -> {
                    var xCord = 0F
                    var yCord = 0F

                    repeat(event.pointerCount) {
                        xCord += event.getX(it)
                        yCord += event.getY(it)
                    }

                    xCord /= event.pointerCount
                    yCord /= event.pointerCount

                    val s = (x - xCord).absoluteValue + (y - yCord).absoluteValue


                    binding.image.translationX = xCord - x
                    binding.image.translationY = yCord - y

                    if (event.pointerCount > 1) {
                        action = 1

                        binding.image.alpha = 1F

                        var d = sqrt(
                            (event.getX(1) - event.getX(0)).pow(2)
                                    + (event.getY(1) - event.getY(0)).pow(2)
                        )

                        if (distance != 0F) {

                            val f = d - distance

                            binding.image.scaleX += f / 1500
                            binding.image.scaleY += f / 1500

                            if (binding.image.scaleX < 0.5) {
                                binding.image.scaleX = 0.5F
                                binding.image.scaleY = 0.5F
                            }

                            if (binding.image.scaleX > 2.5) {
                                binding.image.scaleX = 2.5F
                                binding.image.scaleY = 2.5F
                            }

                        }

                        distance = d
                    } else {

                        if (action == 0) {
                            binding.image.alpha = 1 / (s / 150 + 1)
                            binding.image.scaleX = 1 / (s / 350 + 1)
                            binding.image.scaleY = 1 / (s / 350 + 1)
                        }

                        distance = 0F
                    }

                }

                MotionEvent.ACTION_POINTER_UP -> {
                    var xCord = 0F
                    var yCord = 0F

                    repeat(event.pointerCount) {
                        if (it != event.actionIndex) {
                            xCord += event.getX(it)
                            yCord += event.getY(it)
                        }
                    }

                    x = xCord / (event.pointerCount - 1) - binding.image.translationX
                    y = yCord / (event.pointerCount - 1) - binding.image.translationY
                }

                MotionEvent.ACTION_UP -> {

                    if (action == 0 && (binding.image.alpha < 0.1)) {
                        vm.clear()
                    }

                    binding.image.animate()
                        .setDuration(100)
                        .alpha(1F)
                        .translationX(0F)
                        .translationY(0F)
                        .scaleX(1F)
                        .scaleY(1F)
                        .start()

                    val rawX = binding.image.x
                    val rawY = binding.image.y
                    val width = binding.image.width
                    val height = binding.image.height

                    if (action == 0 && (x < rawX || x > rawX + width || y < rawY || y > rawY + height))
                        vm.clear()

                }
                else -> {
                }
            }
            true
        }


        vm.chatId = chatId
        vm.getUsers(chatId)
        vm.observeMessages(chatId)
        vm.getMessages(chatId)
    }

    private fun observeUsers() {
        vm.users.observe(this, Observer { users ->
            adapter.users.clear()
            adapter.users.addAll(users)

            if (title.isEmpty()) vm.title.value = com.tarlad.client.helpers.getTitle(null, users, vm.appSession.userId!!)
        })
    }

    private fun observeMessages() {
        vm.messages.observe(this, Observer { list ->
            list.forEach { pair ->
                val action = pair.first
                val messages = pair.second
                when (action) {
                    Messages.ADD -> adapter.add(messages)
                    Messages.REMOVE -> adapter.remove(messages)
                    Messages.DELETE -> adapter.delete(messages)
                    Messages.UPDATE -> adapter.update(messages)
                    Messages.REPLACE -> adapter.replace(messages)
                    Messages.COMPLETE -> binding.messagesRecycler.clearOnScrollListeners()
                    Messages.SEND -> {
                        adapter.add(messages)
                        binding.messagesRecycler.smoothScrollToPosition(0)
                    }
                }
            }
            vm.messages.value!!.clear()
        })
    }

    private fun observeError() {
        vm.error.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                val snack = Snackbar.make(chat_container, it, Snackbar.LENGTH_LONG)
                snack.setBackgroundTint(
                    ContextCompat.getColor(applicationContext, R.color.colorError)
                )
                snack.show()
            }
        })
        vm.error.value = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_chat_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.open_chat_details -> {
                val intent = Intent(this, ChatDetailsActivity::class.java)
                intent.putExtra("ID", chatId)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    private fun initRecyclerView() {
        adapter = MessagesAdapter(vm.data)
        binding.messagesRecycler.adapter = adapter
        binding.messagesRecycler.itemAnimator =
            MessageItemAnimator()
        binding.messagesRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = messages_recycler.layoutManager!!.itemCount
                val lastVisibleItem =
                    (messages_recycler.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (totalItemCount <= lastVisibleItem + 5)
                    vm.getMessages(chatId)
            }
        })
    }

    private fun initAdapter() {
        adapter.userId = vm.appSession.userId ?: -1
        adapter.deleteListener = { message -> vm.deleteMessage(message) }
        adapter.editListener = { message -> vm.editMessage(message) }
        adapter.clickImageListener = { url -> vm.image.value = url }
    }

    fun openImages(v: View) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.sheet_chat, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetView.take.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
                requestPermissions(arrayOf(Manifest.permission.CAMERA).toList().toTypedArray(), 401)
            else
                dispatchTakePictureIntent()
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.gallery.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(intent, ImagePicker.GALLERY)
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.cancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()

        val bottomSheetDialogFrameLayout =
            bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheetDialogFrameLayout?.background = null
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
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile(this).apply { currentPhotoPath = absolutePath }
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
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
                    contentResolver.let {
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
