package com.example.whatsapp.ui

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.whatsapp.Contact
import com.example.whatsapp.R
import com.example.whatsapp.adapter.ContactAdapter
import com.example.whatsapp.databinding.FragmentMainBinding
import com.example.whatsapp.usecase.MyViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentMainBinding>() {

    private lateinit var adapter: ContactAdapter
    val viewModel: MyViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore

    private lateinit var chatId: String
    private var flag = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadContacts()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_item, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = adapter.filterList(newText)
                adapter.setNewList(filteredList)
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                flag = !flag
                binding.btnSignOut.visibility = if (flag) View.VISIBLE else View.INVISIBLE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        setHasOptionsMenu(true)

        adapter = ContactAdapter { person ->
            val normalPhoneNumber = normalizePhone(person.number.toString())
            sharedPreferences.edit { putString("Name",person.name) }
            firestore.collection("users")
                .whereEqualTo("number", normalPhoneNumber)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val document = snapshot.documents[0]
                        val uid = document.getString("uid")
                        val currentUser = auth.currentUser
                        chatId = getChatId(currentUser!!.uid, uid.toString())
                        findNavController().navigate(
                            HomeFragmentDirections.actionMainFragmentToChatFragment(uid.toString())
                        )
                    } else {
                        Toast.makeText(requireContext(), "User not found in database", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to check user", Toast.LENGTH_SHORT).show()
                }
        }

        binding.rvContacts.adapter = adapter

        binding.btnNewMessage.setOnClickListener {
            checkContactPermission()
        }

        binding.btnSignOut.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.authorizationFragment)
        }

        viewModel.contacts.observe(viewLifecycleOwner) { contactList ->
            val currentUserId = auth.currentUser?.uid ?: return@observe
            attachLastMessagesToContacts(currentUserId, contactList) { updatedContacts ->
                adapter.setData(updatedContacts)
            }
        }
    }

    private fun checkContactPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.loadContacts()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun attachLastMessagesToContacts(
        currentUserId: String,
        contactList: List<Contact>,
        onResult: (List<Contact>) -> Unit
    ) {
        val updatedContacts = contactList.toMutableList()
        var loaded = 0

        if (updatedContacts.isEmpty()) {
            onResult(emptyList())
            return
        }

        for (contact in updatedContacts) {
            val otherUserId = contact.uid
            val chatId = getChatId(currentUserId, otherUserId)

            firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val lastMessage = snapshot.documents.firstOrNull()?.getString("message")
                    contact.lastMessage = lastMessage ?: ""
                }
                .addOnCompleteListener {
                    loaded++
                    if (loaded == updatedContacts.size) {
                        onResult(updatedContacts)
                    }
                }
        }
    }

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    override fun getViewBinding(): FragmentMainBinding {
        return FragmentMainBinding.inflate(layoutInflater)
    }
}

fun normalizePhone(phone: String): String {
    val digitsOnly = phone.replace("[^\\d]".toRegex(), "")
    return when {
        digitsOnly.startsWith("994") -> "+$digitsOnly"
        digitsOnly.startsWith("0") -> "+994" + digitsOnly.substring(1)
        digitsOnly.length == 9 -> "+994$digitsOnly"
        else -> "+$digitsOnly"
    }
}
