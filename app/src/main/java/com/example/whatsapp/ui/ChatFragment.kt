package com.example.whatsapp.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.graphics.Color
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.example.whatsapp.Message
import com.example.whatsapp.R
import com.example.whatsapp.adapter.MessageAdapter
import com.example.whatsapp.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding>() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var adapter: MessageAdapter
    private lateinit var chatId: String
    private var deleteMenuItem: MenuItem? = null
    private var messageId: String? = null
    private var messageView: View? = null

    override fun getViewBinding(): FragmentChatBinding {
        return FragmentChatBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: ChatFragmentArgs by navArgs()
        val receiverId = args.uid
        val senderId = auth.currentUser?.uid!!
        chatId = getChatId(receiverId, senderId)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.chat_item, menu)
                deleteMenuItem = menu.findItem(R.id.delete)
                deleteMenuItem?.isVisible = false
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.delete -> {
                        val id = messageId
                        val view = messageView
                        if (id != null && view != null) {
                            val dialog = AlertDialog.Builder(requireContext())
                                .setTitle("Delete Message")
                                .setMessage("Do you want to delete it?")
                                .setPositiveButton("Yes") { _, _ ->
                                    firestore.collection("chats").document(chatId)
                                        .collection("messages").document(id)
                                        .delete()
                                    view.setBackgroundColor(Color.TRANSPARENT)
                                    messageId = null
                                    messageView = null
                                    deleteMenuItem?.isVisible = false
                                }
                                .setNegativeButton("No") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .create()
                            dialog.setOnDismissListener {
                                view.setBackgroundColor(Color.TRANSPARENT)
                            }
                            dialog.show()
                        }
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        adapter = MessageAdapter(auth) { messageId, rootView ->
            this@ChatFragment.messageId = messageId
            messageView = rootView
            rootView.setBackgroundColor(Color.LTGRAY)
            deleteMenuItem?.isVisible = true
        }

        binding.rvMessages.adapter = adapter

        firestore.collection("chats").document(chatId)
            .collection("messages").orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messageList = snapshot?.documents?.mapNotNull {
                    val message = it.toObject(Message::class.java)
                    if (message != null) it.id to message else null
                } ?: emptyList()

                adapter.setList(messageList)
                binding.rvMessages.scrollToPosition(messageList.size - 1)
            }

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString()
            val messageData = hashMapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "message" to message,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("chats").document(chatId)
                .collection("messages").add(messageData)
                .addOnSuccessListener {
                    binding.etMessage.setText("")
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to send", Toast.LENGTH_SHORT).show()
                }
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            sharedPreferences.getString("Name", "")
    }

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }
}