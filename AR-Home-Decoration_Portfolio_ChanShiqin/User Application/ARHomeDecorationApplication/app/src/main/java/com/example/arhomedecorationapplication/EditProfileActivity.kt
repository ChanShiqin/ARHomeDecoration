package com.example.arhomedecorationapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class EditProfileActivity : AppCompatActivity() {

    private lateinit var userInfoRef: DatabaseReference
    private lateinit var backTextView: TextView
    private lateinit var editUserEmail: TextView
    private lateinit var editUserName: TextView
    private lateinit var editUserPhone: TextView
    private lateinit var editUserAddress: TextView
    private lateinit var editUserPoscode: TextView
    private lateinit var editUserState: Spinner
//    private lateinit var editUserState: TextView
//    private lateinit var editCurrentPassword: TextView
//    private lateinit var editNewPassword: TextView
//    private lateinit var editConfirmNewPassword: TextView
    private lateinit var profilePicture: CircleImageView
    private lateinit var changeImage: TextView
    private lateinit var saveChangesButton: AppCompatButton

    // Firebase reference to user data (Assuming 'users' node in Firebase)
    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        footerActivity()

        // Initialize the views
        userInfoRef = FirebaseDatabase.getInstance().getReference("user")
        backTextView = findViewById(R.id.backTextView)
        editUserEmail = findViewById(R.id.userEmail)
        editUserName = findViewById(R.id.userName)
        editUserPhone = findViewById(R.id.userPhone)
        editUserAddress = findViewById(R.id.userAddress)
        editUserPoscode = findViewById(R.id.userPoscode)
        editUserState = findViewById(R.id.userStateSpinner)
//        editUserState = findViewById(R.id.userState)
//        editCurrentPassword = findViewById(R.id.currentPassword)
//        editNewPassword = findViewById(R.id.newPassword)
//        editConfirmNewPassword = findViewById(R.id.confirmNewPassword)
        profilePicture = findViewById(R.id.userProfilePicture)
        changeImage = findViewById(R.id.changeImageText)
        saveChangesButton = findViewById(R.id.saveChangesButton)

        // Initialize views
        saveChangesButton = findViewById(R.id.saveChangesButton)

        changeImage.setOnClickListener {
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        fetchUserData()

        // Set the onClickListener for the Save Changes button
        saveChangesButton.setOnClickListener {
            saveUserProfile()
        }

//        supportActionBar!!.setAc
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
                        editUserEmail.text = snapshot.child("email").getValue(String::class.java) ?: "No Email"
                        editUserName.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                        editUserPhone.setText(snapshot.child("phoneNo").getValue(String::class.java) ?: "")
                        editUserAddress.setText(snapshot.child("address").getValue(String::class.java) ?: "")
                        editUserPoscode.setText(snapshot.child("poscode").getValue(String::class.java) ?: "")
//                        editUserState.setText(snapshot.child("state").getValue(String::class.java) ?: "")

                        // Load profile picture using Picasso if there’s a URL
                        val profilePictureUrl = snapshot.child("profilePic").getValue(String::class.java)
//                        if (!profilePictureUrl.isNullOrEmpty()) {
//                            Picasso.get().load(profilePictureUrl).into(profilePicture)
//                        }

//                        val productImageView = orderView.findViewById<ImageView>(R.id.productImage)

                        if (!profilePictureUrl.isNullOrEmpty()) {
                            val decodedBitmap = decodeBase64ToBitmap(profilePictureUrl)
                            if (decodedBitmap != null) {
                                profilePicture.setImageBitmap(decodedBitmap)
                            } else {
                                profilePicture.setImageResource(R.drawable.baseline_person_24) // Default image placeholder
                            }
                        } else {
                            profilePicture.setImageResource(R.drawable.baseline_person_24) // Default image placeholder
                        }

                        val states = resources.getStringArray(R.array.states_array)
                        val adapter = object : ArrayAdapter<String>(this@EditProfileActivity, android.R.layout.simple_spinner_item, states) {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getView(position, convertView, parent)
                                (view as TextView).setTextColor(Color.BLACK) // Set text color for selected item
                                return view
                            }

                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getDropDownView(position, convertView, parent)
                                (view as TextView).setTextColor(Color.BLACK) // Set text color for dropdown items
                                return view
                            }
                        }

                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                        editUserState.adapter = adapter

                        // Check if the state exists in the states array
                        val currentState = snapshot.child("state").getValue(String::class.java)
                        if (currentState != null) {
                            val currentStateIndex = states.indexOf(currentState)
                            if (currentStateIndex >= 0) {
                                // Set the selected state in the spinner
                                editUserState.setSelection(currentStateIndex)
                            }
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

    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            Log.e("ImageError", "Error decoding Base64 string: ${e.message}")
            null
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
            val updatedPhoneNo = editUserPhone.text.toString().trim()
            val updatedAddress = editUserAddress.text.toString().trim()
            val updatedPoscode = editUserPoscode.text.toString().trim()
//            val updatedState = editUserState.text.toString().trim()
            val updatedState = editUserState.selectedItem.toString().trim()

            // Check if any required field is empty
            if (updatedName.isEmpty() || updatedPhoneNo.isEmpty() || updatedAddress.isEmpty() || updatedPoscode.isEmpty() || updatedState == "Select State") {
                showToast("Please fill in all fields.")
                return
            }

            // Check if the phone number format is valid (it should be a valid phone number)
            if (!Regex("^[0-9]{10,11}$").matches(updatedPhoneNo)) {
                showToast("Please enter a valid phone number.")
                return
            }

            // Check if postcode is exactly 5 digits long, and allows leading zeros
            if (!Regex("^\\d{5}\$").matches(updatedPoscode)) {
                showToast("Poscode must be exactly 5 digits.")
                return
            }

            if (updatedState == "Select State") {
                Toast.makeText(this, "Please select a state", Toast.LENGTH_SHORT).show()
                return
            }

//            // Retrieve the entered passwords
//            val currentPasswordInput = editCurrentPassword.text.toString().trim()
//            val newPasswordInput = editNewPassword.text.toString().trim()
//            val confirmNewPasswordInput = editConfirmNewPassword.text.toString().trim()

//            if (currentPasswordInput.isEmpty() && newPasswordInput.isEmpty() && confirmNewPasswordInput.isEmpty()) {
//                // No need to update the password if all fields are empty
//                showToast("No password change required.")
//            } else if (!isValidPassword(newPasswordInput)) {
//                showToast("Password must have at least 8 characters, one uppercase letter, one digit, and one special character.")
//                return
//            } else if (newPasswordInput != confirmNewPasswordInput) {
//                showToast("New Password and Confirm Password do not match.")
//                return
//            } else {
//                // Validate that all password fields are filled
//                if (currentPasswordInput.isEmpty() || newPasswordInput.isEmpty() || confirmNewPasswordInput.isEmpty()) {
//                    showToast("Please fill in all password fields.")
//                    return
//                } else {
////                    // Step 1: Fetch the current password from the database
//                    userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            if (snapshot.exists()) {
//                                val currentPasswordInDB = snapshot.child("password").getValue(String::class.java)
//
//                                // Step 2: Check if the current password matches
//                                if (currentPasswordInDB == currentPasswordInput) {
//                                    // Step 3: Validate new password and confirm new password
//                                    if (newPasswordInput == confirmNewPasswordInput) {
//                                        // Step 4: Update the password in the database
//                                        val updates = mapOf("password" to newPasswordInput)
//                                        userRef.child(userId).updateChildren(updates).addOnCompleteListener { task ->
//                                            if (task.isSuccessful) {
//                                                showToast("Password updated successfully!")
//                                            } else {
//                                                showToast("Failed to update password: ${task.exception?.message}")
//                                            }
//                                        }
//                                    } else {
//                                        showToast("New Password and Confirm Password do not match.")
//                                        return
//                                    }
//                                } else {
//                                    showToast("Current Password is incorrect.")
//                                    return
//                                }
//                            } else {
//                                showToast("User data not found.")
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                            showToast("Failed to fetch data: ${error.message}")
//                        }
//                    })
//                }
//            }

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
                    val intent = Intent(this, SettingActivity::class.java)
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

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}"
        return Regex(passwordPattern).matches(password)
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

        val backTextView: TextView = findViewById(R.id.backTextView)
        backTextView.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

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
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            finish()
        }
        settingTextView.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            finish()
        }
        cartIconImageView.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {
            val fileUri = data?.data

            if (fileUri != null) {
                // Display the selected image
                profilePicture.setImageURI(fileUri)

                // Convert image to Base64
                val inputStream = contentResolver.openInputStream(fileUri)
                val byteArray = inputStream?.readBytes()
                inputStream?.close()

                if (byteArray != null) {
                    val base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
                    saveBase64ImageToFirebase(base64Image)
                } else {
                    showToast("Failed to process image")
                }
            }
        } else {
            showToast("Image selection canceled")
        }
    }

    private fun saveBase64ImageToFirebase(base64Image: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null) {
            val updates = mapOf("profilePic" to base64Image)
            userRef.child(userId).updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Profile picture updated successfully!")
                } else {
                    showToast("Failed to update profile picture: ${task.exception?.message}")
                }
            }
        } else {
            showToast("User not found. Please log in again.")
        }
    }


}