package com.keeghan.firechat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.keeghan.firechat.databinding.ActivitySignUpBinding
import com.keeghan.firechat.util.Constants
import com.keeghan.firechat.util.PreferenceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class SignUpActivity : AppCompatActivity() {
    private lateinit var encodedImage: String
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
    }


    private fun setListeners() {
        binding.signInText.setOnClickListener {
            onBackPressed()
        }
        binding.btnSignUp.setOnClickListener {
            if (isInputValid()) {
                signUp()
            }
        }
        binding.imageProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uploadEncodeImage.launch(intent)
        }
    }


    private val uploadEncodeImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            if (it.data != null) {
                val imageUri = it.data!!.data
                try {
                    val inputStream = contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding.imageProfile.setImageBitmap(bitmap)
                    binding.uploadImageTxt.visibility = View.GONE
                    encodedImage = encodeUploadImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun signUp() {
        progressLoadStatus(true)
        val dataBase: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user: HashMap<String, Any> = HashMap()
        user[Constants.KEY_NAME] = binding.inputName.text.toString()
        user[Constants.KEY_EMAIL] = binding.inputEmail.text.toString()
        user[Constants.KEY_PASSWORD] = binding.inputPassword.text.toString()
        user[Constants.KEY_IMAGE] = encodedImage
        dataBase.collection(Constants.KEY_USERS)
            .add(user)
            .addOnSuccessListener {
                progressLoadStatus(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, it.id)
                preferenceManager.putString(Constants.KEY_NAME, binding.inputName.text.toString())
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage)
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
            .addOnFailureListener {
                progressLoadStatus(false)
                showToast(it.message!!)
            }

    }

    private fun encodeUploadImage(bitmap: Bitmap): String {
        val pWidth = 150
        val pHeight: Int = bitmap.height * pWidth / bitmap.width
        val pBitmap = Bitmap.createScaledBitmap(bitmap, pWidth, pHeight, false)
        val byteArray = ByteArrayOutputStream()
        pBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArray)
        val byte = byteArray.toByteArray()
        return Base64.encodeToString(byte, Base64.DEFAULT)

    }

    private fun progressLoadStatus(isLoading: Boolean) {
        if (isLoading) {
            binding.btnSignUp.visibility = View.INVISIBLE
            binding.signUpProgress.visibility = View.VISIBLE
        } else {
            binding.btnSignUp.visibility = View.VISIBLE
            binding.signUpProgress.visibility = View.INVISIBLE
        }
    }


    private fun validateEmail(): Boolean {
        var valid = true
        val dataBase: FirebaseFirestore = FirebaseFirestore.getInstance()
        dataBase.collection(Constants.KEY_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.text.toString())
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                    valid = false
                }
            }
        return valid
    }


    private fun isInputValid(): Boolean {
        return if (!::encodedImage.isInitialized) {
            showToast("Select Profile Image")
            false
        } else if (binding.inputName.text.toString().trim().isEmpty()) {
            showToast("Please Enter Name")
            false
        } else if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Please Enter Name")
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString().trim())
                .matches()
        ) {
            showToast("Enter valid Email")
            false
        } else if (!validateEmail()) {
            showToast("Email Already Taken")
            false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter Password")
            false
        } else if (binding.inputPasswordConfirm.text.toString().trim().isEmpty()) {
            showToast("Confirm Password")
            false
        } else if (binding.inputPasswordConfirm.text.toString() != binding.inputPasswordConfirm.text.toString()
        ) {
            showToast("Confirm Password")
            false
        } else {
            true
        }
    }

    private fun showToast(s: String) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT).show()
    }
}
