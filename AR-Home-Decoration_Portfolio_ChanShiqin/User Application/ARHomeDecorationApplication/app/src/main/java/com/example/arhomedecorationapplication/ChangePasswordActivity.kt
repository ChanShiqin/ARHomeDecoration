package com.example.arhomedecorationapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var saveChangesButton: AppCompatButton
    private lateinit var cancelButton: AppCompatButton
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var progressBar: ProgressBar

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var userRef: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        footerActivity()

        // Initialize views
        currentPasswordEditText = findViewById(R.id.editCurrentPassword)
        newPasswordEditText = findViewById(R.id.editNewPassword)
        confirmPasswordEditText = findViewById(R.id.editConfirmNewPassword)
        saveChangesButton = findViewById(R.id.saveChangesButton)
        cancelButton = findViewById(R.id.cancelButton)

        saveChangesButton.setOnClickListener {
            validateAndSavePassword()
        }

        cancelButton.setOnClickListener {
            // Navigate back to the SettingActivity without saving changes
            val intent = Intent(this, Setting2Activity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateAndSavePassword() {
        val currentPassword = currentPasswordEditText.text.toString()
        val newPassword = newPasswordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill in all fields.")
            return
        }

        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            // Access the user's data from Firebase Database
            val userRef = database.child("user").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Fetch current user's email for reauthentication
                        val userEmail = snapshot.child("email").getValue(String::class.java)

                        if (userEmail != null) {
                            // Get the current Firebase user
                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                // Create the credential for reauthentication
                                val credential = EmailAuthProvider.getCredential(userEmail, currentPassword)

                                // Reauthenticate with the current password
                                currentUser.reauthenticate(credential)
                                    .addOnSuccessListener {
                                        // Now, check if the new password and confirmation are valid
                                        if (newPassword != confirmPassword) {
                                            showToast("New password and confirm password do not match.")
                                        } else if (newPassword.length < 6) {
                                            showToast("Password must be at least 6 characters long.")
                                        } else {
                                            // Update the password in Firebase Auth
                                            currentUser.updatePassword(newPassword)
                                                .addOnSuccessListener {
                                                    showToast("Password changed successfully.")
                                                    startActivity(Intent(this@ChangePasswordActivity, Setting2Activity::class.java))
                                                    finish()
                                                }
                                                .addOnFailureListener {
                                                    showToast("Failed to change password.")
                                                }
                                        }
                                    }
                                    .addOnFailureListener {
                                        showToast("Current password is incorrect.")
                                    }
                            } else {
                                showToast("No current user found.")
                            }
                        }
                    } else {
                        showToast("User data not found.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Failed to retrieve user data: ${error.message}")
                }
            })
        } else {
            showToast("User ID not found. Please log in again.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun footerActivity() {
        val homeIcon: ImageView = findViewById(R.id.homeIcon)
        val homeTextView: TextView = findViewById(R.id.homeTextView)
        val shopIcon: ImageView = findViewById(R.id.shopIcon)
        val shopTextView: TextView = findViewById(R.id.shopTextView)
        val settingIcon: ImageView = findViewById(R.id.settingIcon)
        val settingTextView: TextView = findViewById(R.id.settingTextView)
        val cartIconImageView: ImageView = findViewById(R.id.cartIcon)

        val navigateHeaderTextView: TextView = findViewById(R.id.logoTitleText)
        navigateHeaderTextView.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val navigateBackTextView: TextView = findViewById(R.id.backTextView)
        navigateBackTextView.setOnClickListener {
            // Check if there are any activities in the back stack
            if (supportFragmentManager.backStackEntryCount > 0) {
                onBackPressedDispatcher.onBackPressed() // Go to the previous activity in the back stack
            } else {
                // If there’s no back stack, start a specific activity, e.g., HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish() // Optionally finish the current activity
            }
        }

        homeIcon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        homeTextView.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        shopIcon.setOnClickListener {
            val intent = Intent(this, CategoryListActivity::class.java)
            startActivity(intent)
            finish()
        }
        shopTextView.setOnClickListener {
            val intent = Intent(this, CategoryListActivity::class.java)
            startActivity(intent)
            finish()
        }
        settingIcon.setOnClickListener {
            val intent = Intent(this, Setting2Activity::class.java)
            startActivity(intent)
            finish()
        }
        settingTextView.setOnClickListener {
            val intent = Intent(this, Setting2Activity::class.java)
            startActivity(intent)
            finish()
        }
        cartIconImageView.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }
}
