package com.example.arhomedecorationapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class Setting2Activity : AppCompatActivity() {

    private lateinit var userRef: DatabaseReference
    private lateinit var userEmail: TextView
    private lateinit var userName: TextView
    private lateinit var userPhone: TextView
    private lateinit var userAddress: TextView
    private lateinit var posCode: TextView
    private lateinit var state: TextView
    private lateinit var userProfilePicture: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting2)
        footerActivity()
        logout()

        // Initialize references
        userRef = FirebaseDatabase.getInstance().getReference("user")
        userEmail = findViewById(R.id.userEmail)
        userName = findViewById(R.id.userName)
        userPhone = findViewById(R.id.userPhone)
        userAddress = findViewById(R.id.userAddress)
        posCode = findViewById(R.id.posCode)
        state = findViewById(R.id.state)
        userProfilePicture = findViewById(R.id.userProfilePicture)

        val editProfileButton: Button = findViewById(R.id.editButton)
        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        val changePasswordButton: Button = findViewById(R.id.changePasswordButton)
        // Set the onClickListener for the Cancel button
        changePasswordButton.setOnClickListener {
            // Navigate back to SettingActivity without saving any changes
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
            finish() // Optionally finish the current activity so the user cannot go back to EditProfileActivity
        }

        // Fetch user data using SharedPreferences
        fetchUserData()
    }

    override fun onResume() {
        super.onResume()

        // Reload user data when the activity resumes (e.g., after save operation)
        fetchUserData()
    }

    private fun fetchUserData() {
        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            // Use the retrieved user ID to access the user's data
            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Retrieve data and update UI
                        userEmail.text = snapshot.child("email").getValue(String::class.java) ?: ""
                        userName.text = snapshot.child("name").getValue(String::class.java) ?: ""
                        userPhone.text = snapshot.child("phoneNo").getValue(String::class.java) ?: ""
                        userAddress.text = snapshot.child("address").getValue(String::class.java) ?: ""
                        posCode.text = snapshot.child("poscode").getValue(String::class.java) ?: ""
                        state.text = snapshot.child("state").getValue(String::class.java) ?: ""

                        // Load profile picture using Picasso if there’s a URL
                        val profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String::class.java)
                        if (!profilePictureUrl.isNullOrEmpty()) {
                            Picasso.get().load(profilePictureUrl).into(userProfilePicture)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
        } else {
            // Handle case where user ID is not found, e.g., prompt the user to log in again
            showToast("User not found. Please log in again.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        // Initialize the logout button
        val logoutButton: Button = findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Clear any session data if necessary
            // For example, if you are using SharedPreferences, clear them here

            // Redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            // Finish the SettingActivity so user cannot go back to it
            finish()
        }
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
                onBackPressedDispatcher.onBackPressed()// Go to the previous activity in the back stack
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
