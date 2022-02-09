package com.keeghan.firechat

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.keeghan.firechat.adapters.RecentConversationAdapter
import com.keeghan.firechat.databinding.ActivityMainBinding
import com.keeghan.firechat.listeners.ConversationListener
import com.keeghan.firechat.model.Message
import com.keeghan.firechat.model.User
import com.keeghan.firechat.util.Constants
import com.keeghan.firechat.util.PreferenceManager

class MainActivity : AppCompatActivity() {
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var conversationsList: MutableList<Message>
    private lateinit var adapter: RecentConversationAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setup()
        loadUserInformation()
        getToken()
        setListener()
        listenForConversations()
    }

    private fun setup() {
        conversationsList = ArrayList()
        preferenceManager = PreferenceManager(applicationContext)
        adapter = RecentConversationAdapter(conversationsList, object : ConversationListener {
            override fun onConversationClicked(user: User) {
                val intent = Intent(applicationContext, ChatActivity::class.java)
                intent.putExtra(Constants.KEY_USER_SINGLE, user)
                startActivity(intent)
            }
        })
        binding.recentConvoRecycler.adapter = adapter
        database = FirebaseFirestore.getInstance()
    }


    private fun setListener() {
        binding.btnLogOut.setOnClickListener {
            signOut()
        }
        binding.mainFab.setOnClickListener {
            startActivity(Intent(applicationContext, UserSelectionActivity::class.java))
        }
    }

    private fun listenForConversations() {
        database.collection(Constants.KEY_CONVERSATIONS)
            .whereEqualTo(
                Constants.KEY_SENDER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_CONVERSATIONS)
            .whereEqualTo(
                Constants.KEY_RECEIVER_ID,
                preferenceManager.getString(Constants.KEY_USER_ID)
            )
            .addSnapshotListener(eventListener)
    }


    private fun loadUserInformation() {
        binding.profileName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(
            preferenceManager.getString(Constants.KEY_IMAGE),
            Base64.DEFAULT
        )
        val bitMap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.iconProfile.setImageBitmap(bitMap)
    }

    private fun showToast(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }

    //update token after successful signIn
    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            tokenUpdate(it)
        }
    }

    private val eventListener: EventListener<QuerySnapshot> = EventListener { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            for (docChange in value.documentChanges) {
                if (docChange.type == DocumentChange.Type.ADDED) {
                    val senderID = docChange.document.getString(Constants.KEY_SENDER_ID)!!
                    val receiverID = docChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    val message = Message()
                    message.senderID = senderID
                    message.receiverID = receiverID
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderID)) {
                        message.conversationImage =
                            docChange.document.getString(Constants.KEY_RECEIVER_IMAGE)!!
                        message.conversationUserName =
                            docChange.document.getString(Constants.KEY_RECEIVER_NAME)!!
                        message.conversationId =
                            docChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    } else {
                        message.conversationImage =
                            docChange.document.getString(Constants.KEY_SENDER_IMAGE)!!
                        message.conversationUserName =
                            docChange.document.getString(Constants.KEY_SENDER_NAME)!!
                        message.conversationId =
                            docChange.document.getString(Constants.KEY_SENDER_ID)!!
                    }
                    message.message = docChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                    message.dateObj = docChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    conversationsList.add(message)

                    //if last conversation has been modified update that for recyclerview display
                } else if (docChange.type == DocumentChange.Type.MODIFIED) {
                    for (message in conversationsList) {
                        val senderID = docChange.document.getString(Constants.KEY_SENDER_ID)
                        val receiverId = docChange.document.getString(Constants.KEY_RECEIVER_ID)
                        if (message.senderID == senderID
                            && message.receiverID == receiverId
                        ) {
                            message.message =
                                docChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                            message.dateObj =
                                docChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                            break
                        }
                    }
                }
            }
            conversationsList.sortedWith(compareBy { it.dateObj })
            adapter.notifyDataSetChanged()
            binding.recentConvoRecycler.smoothScrollToPosition(0)
            binding.recentConvoRecycler.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    //Update token on FireStore and Preference
    private fun tokenUpdate(token: String) {
        val dataBase = FirebaseFirestore.getInstance()
        val documentReference = dataBase.collection(Constants.KEY_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)!!
        )
        documentReference.update(Constants.KEY_FIRE_TOKEN, token)
            .addOnFailureListener { showToast("Token Update Failed") }
    }


    private fun signOut() {
        showToast("Signing Out")
        preferenceManager.clear()
        startActivity(Intent(applicationContext, SignInActivity::class.java))
        finish()
    }

//    private fun signOut() {
//        showToast("Signing Out")
//        val docReference = FirebaseFirestore.getInstance().collection(Constants.KEY_USERS)
//            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)
//        val update = HashMap<String, Any>()
//        update[Constants.KEY_FIRE_TOKEN] = FieldValue.delete()
//        docReference.update(update).addOnSuccessListener {
//            preferenceManager.clear()
//            startActivity(Intent(applicationContext, SignInActivity::class.java))
//            finish()
//        }.addOnFailureListener {
//            showToast("Unable to sign Up, Check internet Connectivity")
//        }
//    }
}