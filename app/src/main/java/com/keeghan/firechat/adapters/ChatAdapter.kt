package com.keeghan.firechat.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.keeghan.firechat.databinding.ChatMessageRecievedContainerBinding
import com.keeghan.firechat.databinding.ChatMessageSentContainerBinding
import com.keeghan.firechat.model.Message


class ChatAdapter(
    var messagesList: List<Message>,
    var receiverProfile: Bitmap,
    var senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }


    inner class SentMessageViewHolder(viewBinding: ChatMessageSentContainerBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        private val binding = viewBinding

        fun setData(message: Message) {
            binding.sentMessage.text = message.message
            binding.sentDate.text = message.dateTime
        }
    }

    inner class ReceivedMessageViewHolder(viewBinding: ChatMessageRecievedContainerBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        private val binding = viewBinding
        fun setData(message: Message, receivedProfile: Bitmap) {
            binding.receivedMessage.text = message.message
            binding.receivedDate.text = message.dateTime
            binding.chatProfile.setImageBitmap(receivedProfile)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_SENT) {
            return SentMessageViewHolder(
                ChatMessageSentContainerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return ReceivedMessageViewHolder(
                ChatMessageRecievedContainerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            (holder as SentMessageViewHolder).setData(messagesList[position])
        } else {
            (holder as ReceivedMessageViewHolder).setData(messagesList[position], receiverProfile)
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messagesList[position].senderID == senderId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }
}