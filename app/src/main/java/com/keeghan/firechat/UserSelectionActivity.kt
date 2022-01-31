package com.keeghan.firechat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.keeghan.firechat.adapters.UserAdapter
import com.keeghan.firechat.databinding.ActivityUserSelectionBinding
import com.keeghan.firechat.listeners.UserListener
import com.keeghan.firechat.model.User
import com.keeghan.firechat.util.Constants
import com.keeghan.firechat.util.PreferenceManager

class UserSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserSelectionBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserSelectionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    private fun getUsers() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_USERS)
            .get()
            .addOnCompleteListener {
                loading(false)
                val userID = preferenceManager.getString(Constants.KEY_USER_ID)
                if (it.isSuccessful and !it.result!!.isEmpty) {
                    val users = ArrayList<User>()
                    for (i in it.result!!) {
                        if (userID.equals(i.id)) continue
                        val user = User()
                        user.name = i.getString(Constants.KEY_NAME)!!
                        user.email = i.getString(Constants.KEY_EMAIL)!!
                        user.image = i.getString(Constants.KEY_IMAGE)!!
                        //token issues
                        user.token = i.getString(Constants.KEY_FIRE_TOKEN)!!
                        user.id = i.id
                        users.add(user)
                        Log.e("====", user.name)
                    }
                    if (users.size > 0) {
                        //Handle chatItem Click here
                        val adapter = UserAdapter(users, object : UserListener {
                            override fun onUserClicked(user: User) {
                                val intent = Intent(applicationContext, ChatActivity::class.java)
                                intent.putExtra(Constants.KEY_USER_SINGLE, user)
                                startActivity(intent)
                                finish()
                            }

                        })
                        binding.usersRecycler.adapter = adapter
                        binding.usersRecycler.visibility = View.VISIBLE
                    } else {
                        error()
                    }
                } else {
                    error()
                }
            }
    }

    private fun setListeners() {
        binding.btnBack.setOnClickListener { onBackPressed() }
    }

    private fun error() {
        binding.errorMessage.text = getString(R.string.error_message)
        binding.errorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

}