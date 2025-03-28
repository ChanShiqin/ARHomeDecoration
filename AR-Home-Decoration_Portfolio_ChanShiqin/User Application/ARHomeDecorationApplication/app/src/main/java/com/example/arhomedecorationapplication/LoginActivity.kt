package com.example.arhomedecorationapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import com.example.arhomedecorationapplication.databinding.ActivityLoginBinding
import com.example.arhomedecorationapplication.databinding.ActivityRegisterBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Initialize the binding and set the content view
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Create a SpannableString with ClickableSpan
        val spannableString = SpannableString("Don't have an account? Sign Up")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle the click, for example, navigate to the register page
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }

        // Set the ClickableSpan only on the "Sign Up" part of the text
        spannableString.setSpan(clickableSpan, 23, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply ForegroundColorSpan to change the color of the underlined text
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#000000")), 23, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply the SpannableString to the TextView
        binding.signupText.text = spannableString
        binding.signupText.movementMethod = LinkMovementMethod.getInstance()
        binding.signupText.highlightColor = Color.TRANSPARENT  // Remove underline highlight

        // Set the login button click listener
        binding.loginButton.setOnClickListener {
            login()
        }

        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        forgotPasswordText.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)); finish() }

//        val forgotPasswordText = findViewById(R.id.forgotPasswordText)
    }

    private fun login() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Validate user input
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill in all fields.")
            return
        }

//        // Use FirebaseAuth to sign in the user
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Login successful
//                    showToast("Login successful")
//
//                    // Navigate to the HomeActivity
//                    val intent = Intent(this, HomeActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                } else {
//                    // Login failed
//                    showToast("Login failed: ${task.exception?.message}")
//                }
//            }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the authenticated user's email
                    val authenticatedEmail = auth.currentUser?.email ?: return@addOnCompleteListener

                    // Query the user database to find the matching email
                    database.child("user").orderByChild("email").equalTo(authenticatedEmail)
                        .get()
                        .addOnSuccessListener { dataSnapshot ->
                            if (dataSnapshot.exists()) {
                                for (userSnapshot in dataSnapshot.children) {
                                    val customId = userSnapshot.child("id").value.toString()

                                    // Save custom ID (e.g., C00007) in SharedPreferences
                                    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("user_id", customId) // Save custom ID
                                    editor.apply()

                                    showToast("Login successful")

                                    // Navigate to the HomeActivity
                                    val intent = Intent(this, HomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                    return@addOnSuccessListener
                                }
                            } else {
                                showToast("No user data found for this email")
                            }
                        }
                        .addOnFailureListener {
                            showToast("Failed to fetch user data: ${it.message}")
                        }
                } else {
                    showToast("Login failed: ${task.exception?.message}")
                }
            }

//        // Search for the user in the Firebase database by email
//        database.child("user").orderByChild("email").equalTo(email)
//            .get()
//            .addOnSuccessListener { dataSnapshot ->
//                if (dataSnapshot.exists()) {
//                    // User with the entered email found
//                    for (userSnapshot in dataSnapshot.children) {
//                        val dbPassword = userSnapshot.child("password").value.toString()
//
//                        if (dbPassword == password) {
//                            // Password matches, login successful
//                            showToast("Login successful")
//
//                            // Retrieve the user ID (key) and store it in SharedPreferences
//                            val userId = userSnapshot.key
//                            val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
//                            val editor = sharedPreferences.edit()
//                            editor.putString("user_id", userId)
//                            editor.apply()
//
//                            // Navigate to the HomeActivity
//                            val intent = Intent(this, HomeActivity::class.java) // Replace with your destination activity
//                            startActivity(intent)
//                            finish()
//
//                        } else {
//                            // Password doesn't match
//                            showToast("Incorrect password")
//                        }
//                    }
//                } else {
//                    // No user found with this email
//                    showToast("No account found with this email")
//                }
//            }
//            .addOnFailureListener {
//                // Handle any errors that occur while querying
//                showToast("Failed to login: ${it.message}")
//            }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}