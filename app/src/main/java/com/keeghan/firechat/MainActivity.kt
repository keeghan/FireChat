package com.keeghan.firechat

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.keeghan.firechat.adapters.RecentConversationAdapter
import com.keeghan.firechat.databinding.ActivityMainBinding
import com.keeghan.firechat.model.Message
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
    }

    private fun setup() {
        conversationsList = ArrayList()
        preferenceManager = PreferenceManager(applicationContext)
        adapter = RecentConversationAdapter(conversationsList)
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