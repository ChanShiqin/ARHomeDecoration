package com.example.arhomedecorationapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class EditProfile2Activity : AppCompatActivity() {

    // Declare the views
    private lateinit var userInfoRef: DatabaseReference
    private lateinit var backTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var editUserName: EditText
    private lateinit var editPhoneNo: EditText
    private lateinit var editAddress: EditText
    private lateinit var editPoscode: EditText
    private lateinit var editState: EditText
    private lateinit var saveChangesButton: AppCompatButton
    private lateinit var cancelButton: AppCompatButton
    private lateinit var profilePicture: CircleImageView

    // Firebase reference to user data (Assuming 'users' node in Firebase)
    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("user")

    // Assuming a 'User' data class that matches the structure in Firebase
    data class User(
        var email: String? = null,
        var fullName: String? = null,
        var phoneNo: String? = null,
        var address: String? = null,
        var poscode: String? = null,
        var state: String? = null,
        var profilePictureUrl: String? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile2)

        footerActivity()

        // Initialize the views
        userInfoRef = FirebaseDatabase.getInstance().getReference("user")
        backTextView = findViewById(R.id.backTextView)
        userEmailTextView = findViewById(R.id.userEmail)
        editUserName = findViewById(R.id.editUserName)
        editPhoneNo = findViewById(R.id.editPhoneNo)
        editAddress = findViewById(R.id.editAddress)
        editPoscode = findViewById(R.id.editPoscode)
        editState = findViewById(R.id.editState)
        saveChangesButton = findViewById(R.id.saveChangesButton)
        cancelButton = findViewById(R.id.cancelButton)
        profilePicture = findViewById(R.id.editProfilePicture)

        fetchUserData()

        // Set the onClickListener for the Save Changes button
        saveChangesButton.setOnClickListener {
            saveUserProfile()
        }

        // Set the onClickListener for the Cancel button
        cancelButton.setOnClickListener {
            // Navigate back to SettingActivity without saving any changes
            val intent = Intent(this, Setting2Activity::class.java)
            startActivity(intent)
            finish() // Optionally finish the current activity so the user cannot go back to EditProfileActivity
        }
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
                        userEmailTextView.text = snapshot.child("email").getValue(String::class.java) ?: "No Email"
                        editUserName.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                        editPhoneNo.setText(snapshot.child("phoneNo").getValue(String::class.java) ?: "")
                        editAddress.setText(snapshot.child("address").getValue(String::class.java) ?: "")
                        editPoscode.setText(snapshot.child("poscode").getValue(String::class.java) ?: "")
                        editState.setText(snapshot.child("state").getValue(String::class.java) ?: "")

                        // Load profile picture using Picasso if there’s a URL
                        val profilePictureUrl = snapshot.child("profilePictureUrl").getValue(String::class.java)
                        if (!profilePictureUrl.isNullOrEmpty()) {
                            Picasso.get().load(profilePictureUrl).into(profilePicture)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                    showToast("Failed to load user data: ${error.message}")
                }
            })
        } else {
            // Handle case where user ID is not found, e.g., prompt the user to log in again
            showToast("User not found. Please log in again.")
        }
    }

    // Show Toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveUserProfile() {
        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            // Retrieve the values from the EditText fields
            val updatedName = editUserName.text.toString().trim()
            val updatedPhoneNo = editPhoneNo.text.toString().trim()
            val updatedAddress = editAddress.text.toString().trim()
            val updatedPoscode = editPoscode.text.toString().trim()
            val updatedState = editState.text.toString().trim()

            // Create a map of the updated data
            val updatedUserData = mapOf(
                "name" to updatedName,
                "phoneNo" to updatedPhoneNo,
                "address" to updatedAddress,
                "poscode" to updatedPoscode,
                "state" to updatedState
            )

            // Update the user data in Firebase
            userRef.child(userId).updateChildren(updatedUserData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Show a success message to the user
                    showToast("Profile updated successfully!")

                    // Optionally, navigate back to the SettingActivity and pass userId to reload data
                    val intent = Intent(this, Setting2Activity::class.java)
                    intent.putExtra("user_id", userId) // Pass user ID to reload data in SettingActivity
                    startActivity(intent)
                    finish() // Finish EditProfileActivity
                } else {
                    // Show an error message if the update failed
                    showToast("Failed to update profile: ${task.exception?.message}")
                }
            }
        } else {
            // Handle case where user ID is not found, e.g., prompt the user to log in again
            showToast("User not found. Please log in again.")
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