package com.example.whatsapp.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    lateinit var adapter: MessageAdapter

    @Inject
    lateinit var auth: FirebaseAuth

    lateinit var chatId: String
    override fun getViewBinding(): FragmentChatBinding {
        return FragmentChatBinding.inflate(layoutInflater)
    }

    fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.delete -> {

                true
            }

            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: ChatFragmentArgs by navArgs()
        (requireActivity() as AppCompatActivity).supportActionBar!!.title =
            sharedPreferences.getString("Name", "")
        val receiverId = args.uid
        val senderId = auth.currentUser?.uid
        chatId = getChatId(receiverId, senderId!!)

        adapter = MessageAdapter(auth) { messageId ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Message")
                .setMessage("Do you want to delete it?")
                .setPositiveButton("Yes") { dialog, _ ->
                    firestore.collection("chats").document(chatId)
                        .collection("messages").document(messageId)
                        .delete()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

        }
        binding.rvMessages.adapter = adapter

        firestore.collection("chats").document(chatId)
            .collection("messages").orderBy("timestamp")
            .addSnapshotListener()
            { snapshot, error ->
                val messageList = snapshot?.documents?.mapNotNull {
                    val message =
                        it.toObject(Message::class.java)

                    if (message != null) {
                        it.id to message
                    } else
                        null
                } ?: emptyList()

                adapter.setList(messageList)
                binding.rvMessages.scrollToPosition(messageList.size - 1)
            }

        binding.btnSend.setOnClickListener()
        {
            val message = binding.etMessage.text.toString()
            val messageData = hashMapOf(
                "senderId" to senderId,
                "receiverId" to receiverId,
                "message" to message,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("chats").document(chatId).collection("messages").add(messageData)
                .addOnSuccessListener {
                    binding.etMessage.setText("")
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }
}