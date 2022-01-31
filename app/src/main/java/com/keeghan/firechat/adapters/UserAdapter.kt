package com.keeghan.firechat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.keeghan.firechat.databinding.UserItemBinding
import com.keeghan.firechat.listeners.UserListener
import com.keeghan.firechat.model.User

class UserAdapter(private val users: List<User>, var userListener: UserListener) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemBinding: UserItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        private val binding = itemBinding
        private lateinit var user: User

        fun bind(user: User) {
            binding.userName.text = user.name
            binding.userEmail.text = user.email
            binding.userProfileImage.setImageBitmap(getUserImage(user.image))
            binding.root.setOnClickListener { userListener.onUserClicked(user) }
        }

        private fun getUserImage(image: String): Bitmap {
            val bytes = Base64.decode(image, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.UserViewHolder {
        val view = UserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

}