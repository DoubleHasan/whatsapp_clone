package com.example.whatsapp

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null
)
