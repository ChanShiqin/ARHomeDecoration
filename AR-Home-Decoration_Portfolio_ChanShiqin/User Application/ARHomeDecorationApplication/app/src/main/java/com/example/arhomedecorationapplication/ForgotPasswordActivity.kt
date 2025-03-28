package com.example.arhomedecorationapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.arhomedecorationapplication.databinding.ActivityForgotPasswordBinding
import com.example.arhomedecorationapplication.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var database: DatabaseReference

    private var email = ""

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize the binding and set the content view
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        auth= FirebaseAuth.getInstance()

        binding.resetPasswordButton.setOnClickListener {
            email = binding.emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                binding.emailEditText.error = "Enter email."
                return@setOnClickListener
            }

            binding.resetPasswordButton.visibility = View.GONE
            auth.sendPasswordResetEmail(email).addOnSuccessListener {
                binding.resetPasswordButton.visibility = View.VISIBLE

//                // Get the user ID or email node to update the password in Realtime Database
//                database.child("user").orderByChild("email").equalTo(email).get()
//                    .addOnSuccessListener { snapshot ->
//                        if (snapshot.exists()) {
//                            // Assuming password reset requires a new password stored in your DB
//                            for (child in snapshot.children) {
//                                val userId = child.key // This will give the unique ID
//                                val newPassword = "NEW_PASSWORD" // Retrieve new password securely
//
//                                // Update new password in Realtime Database
//                                database.child("user").child(userId!!).child("password").setValue(newPassword)
//                            }
//                        }
//                    }

                Snackbar.make(
                    binding.root,
                    "Password reset link send to your $email address.",
                    Snackbar.ANIMATION_MODE_SLIDE
                ).show()
            }.addOnFailureListener {
                binding.resetPasswordButton.visibility = View.VISIBLE
                Snackbar.make(
                    binding.root,
                    "Error: ${it.message}",
                    Snackbar.ANIMATION_MODE_SLIDE
                ).show()
            }
        }

        binding.backToLoginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}