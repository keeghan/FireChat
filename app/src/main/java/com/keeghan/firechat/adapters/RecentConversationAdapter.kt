package com.keeghan.firechat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.keeghan.firechat.databinding.RecentChatItemBinding
import com.keeghan.firechat.listeners.ConversationListener
import com.keeghan.firechat.model.Message
import com.keeghan.firechat.model.User

class RecentConversationAdapter(
    private var messagesList: List<Message>,
    var conversationListener: ConversationListener
) : RecyclerView.Adapter<RecentConversationAdapter.ConversationViewHolder>() {


    inner class ConversationViewHolder(viewBinding: RecentChatItemBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        private val binding = viewBinding

        fun setData(message: Message) {
            binding.userProfileImage.setImageBitmap(decodeImage(message.conversationImage))
            binding.userName.text = message.conversationUserName
            binding.recentMessage.text = message.message
            binding.root.setOnClickListener {
                val user = User()
                user.id = message.conversationId
                user.name = message.conversationUserName
                user.image = message.conversationImage
                conversationListener.onConversationClicked(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = RecentChatItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.setData(messagesList[position])
    }

    override fun getItemCount(): Int = messagesList.size

    private fun decodeImage(image: String): Bitmap {
        val bytes = Base64.decode(image, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }


}