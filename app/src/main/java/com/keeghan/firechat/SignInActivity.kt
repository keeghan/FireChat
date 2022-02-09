package com.keeghan.firechat

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.keeghan.firechat.databinding.ActivitySignInBinding
import com.keeghan.firechat.util.Constants
import com.keeghan.firechat.util.PreferenceManager


class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(applicationContext)

        //check if user is signed and get them to main activity
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }


    private fun setListeners() {
        binding.createNewAccountText.setOnClickListener {
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }
        binding.btnSignIn.setOnClickListener {
            if (isInputValid()) {
                signIn()
            }
        }
    }

    /*SignIN method that compares Firestore records to input
    * and writes information to SharedPreferences*/
    private fun signIn() {
        setIsLoadingStatus(true)
        val dataBase: FirebaseFirestore = FirebaseFirestore.getInstance()
        dataBase.collection(Constants.KEY_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.text.toString())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                    val documentSnapShot = it.result!!.documents[0]
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapShot.id)
                    preferenceManager.putString(
                        Constants.KEY_IMAGE,
                        documentSnapShot.getString(Constants.KEY_IMAGE)!!
                    )
                    preferenceManager.putString(
                        Constants.KEY_NAME,
                        documentSnapShot.getString(Constants.KEY_NAME)!!
                    )
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    // Log.e("====", it.exception?.message!!)
                    setIsLoadingStatus(false)
                    showToast("Unable to Sign in")
                }
            }
    }

    /*validate input for sign in*/
    private fun isInputValid(): Boolean {
        return if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Please Enter Email")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString().trim())
                .matches()
        ) {
            showToast("Enter valid Email")
            false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter Password")
            false
        } else {
            true
        }
    }

    /*Set visibility for signIN button after its clicked*/
    private fun setIsLoadingStatus(isLoading: Boolean) {
        if (isLoading) {
            binding.btnSignIn.visibility = View.INVISIBLE
            binding.signInProgress.visibility = View.VISIBLE
        } else {
            binding.btnSignIn.visibility = View.VISIBLE
            binding.signInProgress.visibility = View.INVISIBLE
        }
    }

    private fun showToast(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }
}