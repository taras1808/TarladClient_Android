package com.tarlad.client.ui.views.chat.details

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityChatDetailsBinding
import com.tarlad.client.databinding.SheetDetailsBinding
import com.tarlad.client.helpers.bindText
import com.tarlad.client.ui.adapters.ChatDetailsAdapter
import com.tarlad.client.ui.views.chat.participants.ChatAddParticipantsActivity
import org.koin.androidx.viewmodel.ext.android.viewModel


class ChatDetailsActivity : AppCompatActivity() {

    private lateinit var adapter: ChatDetailsAdapter

    private val vm by viewModel<ChatDetailsViewModel>()

    var chatId: Long = -1

    lateinit var binding: ActivityChatDetailsBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatId = intent.getLongExtra("ID",-1L)

        binding = ActivityChatDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP

        binding.toolbarInclude.toolbarTitle.text = "Details"

        adapter = ChatDetailsAdapter(arrayListOf(), vm.appSession.userId!!, -1) { id -> show(id) }
        binding.participantsRecycler.adapter = adapter

        vm.loadChatTitle(chatId)
        vm.loadUsers(chatId)
        vm.loadAdmin(chatId)

        observeUsers()
        observeAdmin()


        binding.chatTitle.bindText(this, vm.chatTitle)

        vm.chatTitle.observe(this) {
            invalidateOptionsMenu()
        }

        vm.chatTitleSaved.observe(this) {
            binding.chatTitle.clearFocus()
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(binding.chatTitle.windowToken, 0)
            invalidateOptionsMenu()
        }
    }

    private fun show(id: Long) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = SheetDetailsBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(bottomSheetView.root)
        bottomSheetView.delete.setOnClickListener {
            vm.removeParticipant(chatId, id)
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

    fun openAddParticipants(view: View) {
        val intent = Intent(this, ChatAddParticipantsActivity::class.java)
        intent.putExtra("ID", chatId)
        startActivity(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeUsers() {
        vm.users.observe(this) {
            adapter.users.clear()
            adapter.users.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeAdmin() {
        vm.admin.observe(this) {
            adapter.adminId = it
            adapter.notifyDataSetChanged()
        }
    }

    fun showLeaveAlertDialog(v: View) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.leave_chat))
            .setMessage(getString(R.string.do_you_really_want_to_leave))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                vm.leaveChat(chatId)
            }
            .setNegativeButton(android.R.string.cancel, null).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (vm.chatTitle.value != vm.chatTitleSaved.value)
            menuInflater.inflate(R.menu.menu_confirm, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_confirm -> vm.changeTitle(chatId)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }
}
