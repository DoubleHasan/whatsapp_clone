package com.example.whatsapp.usecase

import com.example.whatsapp.Contact

interface ContactRepository {
    suspend fun getContacts(): List<Contact>
}