package com.tarlad.client.ui.views.chat.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tarlad.client.R
import com.tarlad.client.databinding.ActivityChatDetailsBinding
import com.tarlad.client.ui.adapters.ChatDetailsAdapter
import com.tarlad.client.ui.views.chat.participants.ChatAddParticipantsActivity
import kotlinx.android.synthetic.main.sheet_details.view.*
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class ChatDetailsActivity : AppCompatActivity() {

    private lateinit var adapter: ChatDetailsAdapter

    private val vm by viewModel<ChatDetailsViewModel> { parametersOf(lifecycleScope.id) }

    var chatId: Long = -1

    lateinit var binding: ActivityChatDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatId = intent.getLongExtra("ID",-1L)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_details)
        binding.vm = vm
        binding.lifecycleOwner = this

        setSupportActionBar(binding.toolbarInclude.toolbar)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP

        vm.toolbarTitle.value = "Details"


        adapter = ChatDetailsAdapter(arrayListOf(), vm.appSession.userId!!, -1) { id -> show(id) }
        binding.participantsRecycler.adapter = adapter

        vm.loadChatTitle(chatId)
        vm.loadUsers(chatId)
        vm.loadAdmin(chatId)

        observeUsers()
        observeAdmin()

        vm.chatTitle.observe(this, {
            invalidateOptionsMenu()
        })

        vm.chatTitleSaved.observe(this, {
            binding.chatTitle.clearFocus()
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(binding.chatTitle.windowToken, 0)
            invalidateOptionsMenu()
        })
    }

    private fun show(id: Long) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.sheet_details, null)
        bottomSheetDialog.setContentView(bottomSheetView)
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

    fun openAddParticipants(v: View) {
        val intent = Intent(this, ChatAddParticipantsActivity::class.java)
        intent.putExtra("ID", chatId)
        startActivity(intent)
    }

    private fun observeUsers() {
        vm.users.observe(this , Observer {
            adapter.users.clear()
            adapter.users.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun observeAdmin() {
        vm.admin.observe(this, Observer {
            adapter.adminId = it
            adapter.notifyDataSetChanged()
        })
    }

    fun showLeaveAlertDialog(v: View) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.leave_chat))
            .setMessage(getString(R.string.do_you_really_want_to_leave))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                vm.leaveChat(chatId)
            }
            .setNegativeButton(android.R.string.no, null).show()
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
