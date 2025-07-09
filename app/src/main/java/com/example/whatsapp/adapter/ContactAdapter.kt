package com.example.whatsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.whatsapp.R
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.whatsapp.Contact
import com.example.whatsapp.databinding.ContactItemBinding

class ContactAdapter(private val onClicked: (Contact) -> Unit) :
    RecyclerView.Adapter<ContactViewHolder>() {
    private var contactList: List<Contact> = emptyList()
    private var contactListFull: List<Contact> = emptyList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ContactViewHolder(ContactItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(
        holder: ContactViewHolder,
        position: Int
    ) {
        val currentContact = contactList[position]
        holder.binding.tvName.text = currentContact.name
        holder.binding.tvLastMessage.text =
            if (!currentContact.lastMessage.isNullOrEmpty())
                currentContact.lastMessage
            else
                ""

        Glide.with(holder.itemView.context)
            .load(currentContact.photoUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .circleCrop()
            .into(holder.binding.ivContact)

        holder.binding.root.setOnClickListener {
            onClicked(currentContact)
        }

    }

    fun filterList(query: String?): List<Contact> {
        return if (query!!.isEmpty()) {
            contactListFull.toList()
        } else {
            contactListFull.filter {
                it.name!!.contains(query, ignoreCase = true) ||
                        it.number!!.contains(query, ignoreCase = true)
            }
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    fun setNewList(newList: List<Contact>) {
        contactList = newList
        notifyDataSetChanged()
    }

    fun setData(fullList: List<Contact>) {
        contactListFull = fullList
        contactList = fullList
        notifyDataSetChanged()
    }
}

class ContactViewHolder(val binding: ContactItemBinding) : RecyclerView.ViewHolder(binding.root)