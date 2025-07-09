package com.example.whatsapp.usecase

import com.example.whatsapp.Contact
import javax.inject.Inject

class ContactUseCase @Inject constructor(private val repository: ContactRepository) {

    suspend operator fun invoke() : List<Contact>{
        return repository.getContacts()
    }
}