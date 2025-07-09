package com.example.whatsapp.usecase

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.example.whatsapp.Contact
import com.example.whatsapp.ui.normalizePhone
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ContactUseCaseImpl @Inject constructor(@ApplicationContext private val context: Context) :
    ContactRepository {

    override suspend fun getContacts(): List<Contact> {
        val contentResolver = context.contentResolver
        val contactList = mutableListOf<Contact>()
        val numbersSet = mutableSetOf<String>()

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = normalizePhone(it.getString(numberIndex))
                val photoUrl = it.getString(photoIndex)

                if (number !in numbersSet) {
                    numbersSet.add(number)
                    contactList.add(
                        Contact(
                            uid = "",
                            name = name,
                            number = number,
                            photoUrl = photoUrl
                        )
                    )
                }
            }
        }

        val usersSnapshot = com.google.firebase.firestore.FirebaseFirestore
            .getInstance()
            .collection("users")
            .get()
            .await()

        val firebaseUsers = usersSnapshot.documents.mapNotNull {
            val numberRaw = it.getString("number")
            val uid = it.getString("uid")
            val photoUrl = it.getString("photoUrl")

            if (numberRaw != null && uid != null) {
                val number = normalizePhone(numberRaw)
                Triple(number, uid, photoUrl)
            } else null
        }


        val finalContacts = contactList.map { contact ->
            val match = firebaseUsers.find { it.first == contact.number }
            if (match != null) {
                contact.copy(uid = match.second, photoUrl = match.third ?: contact.photoUrl)
            } else contact
        }

        finalContacts.forEach {
            Log.d("ContactUseCase", "Contact: ${it.name}, Number: ${it.number}, UID: ${it.uid}")
        }

        return finalContacts
    }


}