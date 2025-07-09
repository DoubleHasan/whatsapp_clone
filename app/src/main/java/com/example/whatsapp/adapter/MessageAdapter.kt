package com.example.whatsapp.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.Message
import com.example.whatsapp.databinding.MessageItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.whatsapp.R
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class MessageAdapter @Inject constructor(var auth: FirebaseAuth) :
    RecyclerView.Adapter<MessageViewHolder>() {
    var messageList: List<Message> = emptyList()
    private val currentId = auth.currentUser?.uid
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MessageViewHolder(MessageItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int
    ) {
        val currentMessage = messageList[position]
        val date = currentMessage.timestamp?.toDate()
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.binding.tvTime.text = if (date != null) {
            formatter.format(date)
        } else {
            "--:--"
        }

        holder.binding.tvMessage.text = currentMessage.message.toString()
        if (currentMessage.senderId == currentId) {
            holder.binding.tvMessage.gravity = Gravity.END
            holder.binding.tvMessage.setBackgroundResource(R.drawable.bg_sent)
            holder.binding.tvMessage.setTextColor(Color.BLACK)
            val constraintSet = ConstraintSet()
            constraintSet.clone(holder.binding.root)

            constraintSet.clear(R.id.tvMessage, ConstraintSet.START)
            constraintSet.connect(
                R.id.tvMessage,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )

            constraintSet.clear(R.id.tvTime, ConstraintSet.START)
            constraintSet.connect(R.id.tvTime, ConstraintSet.END, R.id.tvMessage, ConstraintSet.END)

            constraintSet.applyTo(holder.binding.root)
        } else {
            holder.binding.tvMessage.gravity = Gravity.START
            holder.binding.tvMessage.setBackgroundResource(R.drawable.bg_received)
            holder.binding.tvMessage.setTextColor(Color.BLACK)

            val constraintSet = ConstraintSet()
            constraintSet.clone(holder.binding.root)

            constraintSet.clear(R.id.tvMessage, ConstraintSet.START)
            constraintSet.clear(R.id.tvMessage, ConstraintSet.END)
            constraintSet.connect(
                R.id.tvMessage,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.setHorizontalBias(R.id.tvMessage, 0f)

            constraintSet.clear(R.id.tvTime, ConstraintSet.START)
            constraintSet.clear(R.id.tvTime, ConstraintSet.END)
            constraintSet.connect(R.id.tvTime, ConstraintSet.END, R.id.tvMessage, ConstraintSet.END)

            constraintSet.applyTo(holder.binding.root)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun setList(newlist: List<Message>) {
        messageList = newlist
        notifyDataSetChanged()
    }

}

class MessageViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root)