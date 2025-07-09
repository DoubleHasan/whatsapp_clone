package com.example.whatsapp

data class Contact (
    val uid : String = "",
    val name: String?,
    val number: String?,
    val photoUrl: String?,
    var lastMessage : String? = null
)
