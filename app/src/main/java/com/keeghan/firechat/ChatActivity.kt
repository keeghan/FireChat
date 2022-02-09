package com.keeghan.firechat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.keeghan.firechat.adapters.ChatAdapter
import com.keeghan.firechat.databinding.ActivityChatBinding
import com.keeghan.firechat.model.Message
import com.keeghan.firechat.model.User
import com.keeghan.firechat.util.Constants
import com.keeghan.firechat.util.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivityChatBinding
    private lateinit var intentUser: User
    private lateinit var messagesList: MutableList<Message>
    private lateinit var adapter: ChatAdapter
    private lateinit var database: FirebaseFirestore
    private var conversationID: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        intentUser = intent.extras?.get(Constants.KEY_USER_SINGLE) as User
        setListener()
        loadIntentUser()
        setup()
        listenForMessage()
    }

    private fun setup() {
        messagesList = ArrayList()
        adapter = ChatAdapter(
            messagesList,
            getBitmapFromString(intentUser.image),
            preferenceManager.getString(Constants.KEY_USER_ID)!!
        )
        binding.chatRecycler.adapter = adapter
        database = FirebaseFirestore.getInstance()
    }

    private fun getBitmapFromString(encodedBitmap: String): Bitmap {
        val byte = Base64.decode(encodedBitmap, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byte, 0, byte.size)
    }

    private fun loadIntentUser() {
        intentUser = intent.getSerializableExtra(Constants.KEY_USER_SINGLE) as User
        binding.chatUserName.text = intentUser.name
    }

    private fun setListener() {
        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.sendLayout.setOnClickListener { sendMessage() }
    }


    private fun listenForMessage() {
        database.collection(Constants.KEY_CHAT_DATA)
            .whereEqualTo(
                Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .whereEqualTo(Constants.KEY_RECEIVER_ID, intentUser.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_CHAT_DATA)
            .whereEqualTo(Constants.KEY_SENDER_ID, intentUser.id)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
    }


    private val eventListener: EventListener<QuerySnapshot> = EventListener { value, error ->
        if (error != null) {
            return@EventListener
        }

        if (value != null) {
            val count = messagesList.size
            for (docChange in value.documentChanges) {
                if (docChange.type == DocumentChange.Type.ADDED) {
                    val message = Message()
                    message.senderID = docChange.document.getString(Constants.KEY_SENDER_ID)!!
                    message.receiverID = docChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    message.message = docChange.document.getString(Constants.KEY_MESSAGE).toString()
                    message.dateTime =
                        convertDate(docChange.document.getDate(Constants.KEY_TIMESTAMP)!!)
                    message.dateObj = docChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    messagesList.add(message)
                }
            }
            messagesList.sortedWith(compareBy { it.dateObj })
            if (count == 0) {
                adapter.notifyDataSetChanged()
            } else {
                adapter.notifyItemRangeInserted(messagesList.size, messagesList.size)
                binding.chatRecycler.smoothScrollToPosition(messagesList.size - 1)
            }
            binding.chatRecycler.visibility = View.VISIBLE
        }
        binding.chatProgress.visibility = View.GONE
        if (conversationID == null) {
            checkForConversation()
        }
    }


    private fun addConversation(conversation: HashMap<String, Any>) {
        database.collection(Constants.KEY_CONVERSATIONS)
            .add(conversation)
            .addOnSuccessListener { conversationID = it.id }
    }

    private fun updateConversation(message: String) {
        val docReference = database.collection(Constants.KEY_CONVERSATIONS)
            .document(conversationID!!)
        docReference.update(
            Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, Date()
        )
    }

    private fun sendMessage() {
        val message = HashMap<String, Any>()
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)!!
        message[Constants.KEY_RECEIVER_ID] = intentUser.id
        message[Constants.KEY_MESSAGE] = binding.messageInput.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        database.collection(Constants.KEY_CHAT_DATA).add(message)
        if (conversationID != null) {
            updateConversation(binding.messageInput.text.toString())
        } else {
            //formulate a new conversation using sender and receiver information
            val conversation: HashMap<String, Any> = HashMap()
            conversation[Constants.KEY_SENDER_ID] =
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            conversation[Constants.KEY_SENDER_NAME] =
                preferenceManager.getString(Constants.KEY_NAME)!!
            conversation[Constants.KEY_SENDER_IMAGE] =
                preferenceManager.getString(Constants.KEY_IMAGE)!!
            conversation[Constants.KEY_RECEIVER_ID] = intentUser.id
            conversation[Constants.KEY_RECEIVER_NAME] = intentUser.name
            conversation[Constants.KEY_RECEIVER_IMAGE] = intentUser.image
            conversation[Constants.KEY_LAST_MESSAGE] = binding.messageInput.text.toString()
            conversation[Constants.KEY_TIMESTAMP] = Date()
            addConversation(conversation)
        }
        binding.messageInput.text = null
    }

    private fun convertDate(date: Date): String {
        return SimpleDateFormat("MMMM dd - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun checkForConversation() {
        if (messagesList.size != 0) {
            checkForConversationRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID)!!,
                intentUser.id
            )
            checkForConversationRemotely(
                intentUser.id,
                preferenceManager.getString(Constants.KEY_USER_ID)!!
            )
        }
    }

    private fun checkForConversationRemotely(senderId: String, receiverId: String) {
        database.collection(Constants.KEY_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener { conversationCompleteListener }
    }

    private val conversationCompleteListener: OnCompleteListener<QuerySnapshot> =
        OnCompleteListener {
            if (it.isSuccessful && it.result != null
                && it.result!!.documents.size > 0
            ) {
                val documentSnapshot = it.result!!.documents[0]
                conversationID = documentSnapshot.id
            }
        }

}