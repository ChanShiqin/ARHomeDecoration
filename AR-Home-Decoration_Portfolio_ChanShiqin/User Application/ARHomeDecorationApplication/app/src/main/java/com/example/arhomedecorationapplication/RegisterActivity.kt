package com.example.arhomedecorationapplication

import android.R
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
import android.widget.ArrayAdapter
import com.example.arhomedecorationapplication.databinding.ActivityRegisterBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Create a SpannableString with ClickableSpan
        val spannableString = SpannableString("Already have account? Login")
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle the click, for example, navigate to the login page
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        // Set the ClickableSpan only on the "Login" part of the text
        spannableString.setSpan(clickableSpan, 22, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply ForegroundColorSpan to change the color of the underlined text
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#000000")), 22, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply the SpannableString to the TextView
        binding.loginText.text = spannableString
        binding.loginText.movementMethod = LinkMovementMethod.getInstance()
        binding.loginText.highlightColor = Color.TRANSPARENT  // Remove underline highlight

        // Go to login page after register
        binding.signupButton.setOnClickListener {
            registerUser()
        }

        // Define the states array
        val states = arrayOf("Select State", "Johor", "Kedah", "Kelantan", "Melaka", "Negeri Sembilan", "Pahang", "Perak", "Perlis", "Pulau Pinang", "Sabah", "Sarawak", "Selangor", "Terengganu", "Kuala Lumpur", "Labuan", "Putrajaya") // Add more states as needed

        // Get reference to Spinner
        val stateSpinner = binding.stateSpinner

        // Create an ArrayAdapter using a simple spinner layout
        val adapter = ArrayAdapter(this, com.example.arhomedecorationapplication.R.layout.spinner_item, states)

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        stateSpinner.adapter = adapter
    }

    private fun registerUser() {
        // Get user input from EditText fields
        val name = binding.nameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val phoneNo = binding.phoneNoEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
        val address = binding.addressEditText.text.toString().trim()
        val poscode = binding.poscodeEditText.text.toString().trim()
        val selectedState = binding.stateSpinner.selectedItem.toString().trim()
        //        val state = binding.stateEditText.text.toString().trim()

        // Validate user input
//        if (name.isEmpty() || email.isEmpty() || phoneNo.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || address.isEmpty() || poscode.isEmpty() || state.isEmpty()) {
        if (name.isEmpty() || email.isEmpty() || phoneNo.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || address.isEmpty() || poscode.isEmpty() || selectedState.isEmpty()) {
            showToast("Please fill in all fields.")
            return
        }

        if (!Regex("(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}").matches(password)) {
            showToast("Password must have at least 8 characters, one uppercase letter, one digit, and one special character.")
            return
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match.")
            return
        }

        if (!Regex("^\\d{5}\$").matches(poscode)) {
            showToast("Poscode must be exactly 5 digits.")
            return
        }

        if (selectedState == "Select State") {
            Toast.makeText(this, "Please select a state", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if email or phone already exists
        database.child("user").orderByChild("email").equalTo(email).get().addOnSuccessListener { emailSnapshot ->
            if (emailSnapshot.exists()) {
                showToast("Email is already registered.")
                return@addOnSuccessListener
            }


        }

        database.child("user").get().addOnSuccessListener { snapshot ->
            var phoneExists = false

            for (userSnapshot in snapshot.children) {
                val phone = userSnapshot.child("phoneNo").getValue(String::class.java)
                if (phone == phoneNo) {
                    phoneExists = true
                    break
                }
            }

            if (phoneExists) {
                showToast("Phone number is already registered.")
                return@addOnSuccessListener
            }
            else
            {
                // Create user in Firebase Authentication first
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            // Get the last used user ID from Firebase and generate a new one
                            val userRef = database.child("user")
                            userRef.get().addOnSuccessListener { snapshot ->
                                var lastUserId = "C00000" // Start with C00000 if no user exists yet

                                // Find the last user ID by checking existing user IDs
                                snapshot.children.forEach { userSnapshot ->
                                    val userId = userSnapshot.child("id").getValue(String::class.java)
                                    if (userId != null && userId.startsWith("C")) {
                                        if (userId > lastUserId) {
                                            lastUserId = userId
                                        }
                                    }
                                }

                                // Generate new user ID by incrementing the last user ID
                                val newUserId = generateNextUserId(lastUserId)

                                // Create user data object with the new ID
                                val user = User(
                                    newUserId,
                                    name,
                                    email,
                                    phoneNo,
                                    password,
                                    address,
                                    poscode,
                                    selectedState
                                )
                                //            val user = User(newUserId, name, email, phoneNo, password, address, poscode, state)

                                // Save user data to Firebase with custom ID
                                userRef.child(newUserId).setValue(user).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        showToast("Registration successful")

                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        showToast("Registration failed: ${task.exception?.message}")
                                    }
                                }
                            }
                        } else {
                            showToast("Authentication failed: ${task.exception?.message}")
                        }
                    }
            }

//            // Proceed with user creation logic here
//            saveNewUser()
        }.addOnFailureListener {
            showToast("Failed to check phone number: ${it.message}")
            return@addOnFailureListener
        }

    }

    // Helper function to generate the next user ID
    private fun generateNextUserId(lastUserId: String): String {
        val numberPart = lastUserId.substring(1).toInt()
        val newNumber = numberPart + 1
        return "C" + newNumber.toString().padStart(5, '0')
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// Define User data class to represent user data
data class User(
    val id: String?,
    val name: String,
    val email: String,
    val phoneNo: String,
    val password: String,
    val address: String,
    val poscode: String,
    val state: String
)