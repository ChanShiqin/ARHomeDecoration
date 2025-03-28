package com.example.arhomedecorationapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.TypedValueCompat.dpToPx
import com.google.firebase.database.*

class ShareableFolderDetailsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var productRef: DatabaseReference
    private lateinit var folderId: String
    private lateinit var userReference: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shareable_folder_details)
        footerActivity()

        database = FirebaseDatabase.getInstance().reference
        productRef = FirebaseDatabase.getInstance().getReference("product")

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        userReference = FirebaseDatabase.getInstance().getReference("user/$userId")

        // Get the folder ID from the intent
        folderId = intent.getStringExtra("FOLDER_ID") ?: ""
        if (folderId.isEmpty()) {
            Toast.makeText(this, "Invalid folder ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Populate the folder details
        populateFolderDetails()

        // Set up button to show the dialog
        val addPersonButton: ImageButton = findViewById(R.id.addPersonButton)
        addPersonButton.setOnClickListener {
            showAddPersonDialog()
        }

        // Set up the delete folder button
        val deleteFolderButton: ImageButton = findViewById(R.id.deleteFolderButton)
        deleteFolderButton.setOnClickListener {
            confirmDeleteFolder()
        }

    }

    private fun confirmDeleteFolder() {
        // Show confirmation dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete Folder")
            .setMessage("Are you sure you want to delete this folder?")
            .setPositiveButton("Yes") { _, _ ->
                deleteFolder()
            }
            .setNegativeButton("No", null)
            .create()
        dialog.show()
    }

    private fun deleteFolder() {
        val userFolderRef = database.child("user").child(userId!!).child("folder")
        userFolderRef.orderByChild("folderId").equalTo(folderId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (folderSnapshot in snapshot.children) {
                    folderSnapshot.ref.removeValue()
                }

                val folderRef = database.child("userfolder").child(folderId)
                folderRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@ShareableFolderDetailsActivity, "Folder deleted successfully", Toast.LENGTH_SHORT).show()

                        // Navigate back to ShareableFolder activity and refresh it
                        val intent = Intent(this@ShareableFolderDetailsActivity, ShareableFolderActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish() // Finish current activity
                    } else {
                        Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to delete folder", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to check user folder", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun showAddPersonDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_person, null)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Person")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val email = emailEditText.text.toString().trim()
                if (email.isNotEmpty()) {
                    checkEmailAndAddUser(email)
                } else {
                    Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun checkEmailAndAddUser(email: String) {
        val userRef = database.child("user").orderByChild("email").equalTo(email)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Email found, get userId and add folder to user
                    val userId = snapshot.children.first().key ?: return
                    val userEmail = snapshot.children.first().child("email").getValue(String::class.java) ?: ""
                    addFolderToUser(userId, userEmail)
                } else {
                    Toast.makeText(this@ShareableFolderDetailsActivity, "Email not found in database", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to check email", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addFolderToUser(userId: String, userEmail: String) {
        val folderRef = database.child("userfolder").orderByChild("folderId").equalTo(folderId)

        folderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(folderSnapshot: DataSnapshot) {
                if (folderSnapshot.exists()) {
                    // Folder found, update `folderUsers` with userEmail and userId
                    for (folder in folderSnapshot.children) {
                        val folderUsersRef = folder.child("folderUsers")
                        val newUser = hashMapOf(
                            "userEmail" to userEmail,
                            "userId" to userId
                        )

                        // Add new user to folderUsers
                        folderUsersRef.ref.push().setValue(newUser)

                        // Now add folderId to the user's folder list
                        val userFolderRef = database.child("user").child(userId).child("folder")
                        userFolderRef.push().setValue(mapOf("folderId" to folderId))

                        Toast.makeText(this@ShareableFolderDetailsActivity, "Folder shared with $userEmail", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to share folder", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateFolderDetails() {
        val folderRef = database.child("userfolder")

        folderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear previous products before adding new ones
                val productContainer: LinearLayout = findViewById(R.id.productContainer)
                productContainer.removeAllViews()

                if (snapshot.exists()) {
                    for (folderSnapshot in snapshot.children) {
                        val folderIdFromDb = folderSnapshot.child("folderId").getValue(String::class.java) ?: ""
                        val folderName = folderSnapshot.child("folderName").getValue(String::class.java) ?: ""
                        val folderItems = folderSnapshot.child("folderItems")

                        // Check if the folderId matches the current folderId
                        if (folderIdFromDb == folderId) {
                            // Set the folder name in the UI
                            val folderNameTextView: TextView = findViewById(R.id.folderName)
                            folderNameTextView.text = folderName

                            // Loop through the folderItems and display them
                            for (itemSnapshot in folderItems.children) {
                                val itemName = itemSnapshot.child("itemName").getValue(String::class.java) ?: "Unknown Product"
                                val itemId = itemSnapshot.child("itemId").getValue(String::class.java) ?: ""

                                // Now fetch the product details using the itemId
                                productRef.orderByChild("id").equalTo(itemId).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(productSnapshot: DataSnapshot) {
                                        if (productSnapshot.exists()) {
                                            val productData = productSnapshot.children.first()
                                            val productName = productData.child("productName").getValue(String::class.java) ?: "Unknown Product"
                                            val productImages = productData.child("productImages").children.map { it.getValue(String::class.java) ?: "" }
                                            val productSpecs = productData.child("productSpecs").children

                                            val lowestPrice = productSpecs.map {
                                                it.child("productPrice").getValue(Double::class.java) ?: Double.MAX_VALUE
                                            }.minOrNull() ?: 0.0

                                            val productImage = productImages.getOrNull(0) ?: ""

                                            val productBoxView = LayoutInflater.from(this@ShareableFolderDetailsActivity)
                                                .inflate(R.layout.folder_product_box, null)

                                            val layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                            layoutParams.setMargins(0, 0, 0, dpToPx(10))
                                            productBoxView.layoutParams = layoutParams

                                            val productTitle: TextView = productBoxView.findViewById(R.id.productTitle)
                                            val productPrice: TextView = productBoxView.findViewById(R.id.productPrice)
                                            val productImageView: ImageView = productBoxView.findViewById(R.id.productImage)

                                            productTitle.text = productName
                                            productPrice.text = "Price: $lowestPrice"

                                            if (productImage.isNotEmpty()) {
                                                val bitmap = decodeBase64ToBitmap(productImage)
                                                productImageView.setImageBitmap(bitmap)
                                            }

                                            productContainer.addView(productBoxView)

                                            // Add click listener to navigate to ProductDetailsActivity
                                            productBoxView.setOnClickListener {
                                                val intent = Intent(this@ShareableFolderDetailsActivity, ProductDetailsActivity::class.java)
                                                intent.putExtra("productId", itemId)
                                                startActivity(intent)
                                            }

                                            // Add delete button functionality
                                            val deleteButton: ImageView = productBoxView.findViewById(R.id.deleteButton)
                                            deleteButton.setOnClickListener {
                                                deleteProductFromFolder(itemId)
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to load product details", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@ShareableFolderDetailsActivity, "No data found for the folder", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to load folder details", Toast.LENGTH_SHORT).show()
            }
        })

//        folderRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    for (folderSnapshot in snapshot.children) {
//                        val folderUsers = folderSnapshot.child("folderUsers").children
//                        val userContainer: LinearLayout = findViewById(R.id.userContainer)
//                        userContainer.removeAllViews()
//
//                        // Loop through the folder users and add them to the user container
//                        for (userSnapshot in folderUsers) {
//                            val userEmail = userSnapshot.child("userEmail").getValue(String::class.java) ?: continue
//                            val userInitial = userEmail.split("@")[0].take(1).uppercase() // Get the first letter of the email
//
//                            val userView = LayoutInflater.from(this@ShareableFolderDetailsActivity)
//                                .inflate(R.layout.user_added_view, null)
//
//                            val userTextView: TextView = userView.findViewById(R.id.userInitial) // Find the TextView by ID
//                            userTextView.text = userInitial // Set the user's initial
//
//                            // When the user initial is clicked, show the full email
//                            userTextView.setOnClickListener {
//                                Toast.makeText(this@ShareableFolderDetailsActivity, userEmail, Toast.LENGTH_SHORT).show()
//                            }
//
//                            // Add the view to the user container
//                            userContainer.addView(userView)
//                        }
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to load folder details", Toast.LENGTH_SHORT).show()
//            }
//        })
    }

    private fun deleteProductFromFolder(itemId: String) {
        val folderRef = database.child("userfolder").orderByChild("folderId").equalTo(folderId)

        folderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(folderSnapshot: DataSnapshot) {
                if (folderSnapshot.exists()) {
                    for (folder in folderSnapshot.children) {
                        val folderItemsRef = folder.child("folderItems")

                        // Find and remove the item with the matching itemId
                        for (itemSnapshot in folderItemsRef.children) {
                            val currentItemId = itemSnapshot.child("itemId").getValue(String::class.java)
                            if (currentItemId == itemId) {
                                itemSnapshot.ref.removeValue()  // Delete the item from folder

                                // Optionally show a toast or a message
                                Toast.makeText(this@ShareableFolderDetailsActivity, "Product removed from folder", Toast.LENGTH_SHORT).show()
                                break
                            }
                        }
                    }
                    // After deleting, refresh the folder details
                    populateFolderDetails()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to delete product", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    // Helper function to decode Base64 string into Bitmap
    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedString = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }

    private fun footerActivity() {
        val backTextView: TextView = findViewById(R.id.backTextView)
        val homeIcon: ImageView = findViewById(R.id.homeIcon)
        val homeTextView: TextView = findViewById(R.id.homeTextView)
        val shopIcon: ImageView = findViewById(R.id.shopIcon)
        val shopTextView: TextView = findViewById(R.id.shopTextView)
        val settingIcon: ImageView = findViewById(R.id.settingIcon)
        val settingTextView: TextView = findViewById(R.id.settingTextView)
        val cartIconImageView: ImageView = findViewById(R.id.cartIcon)

        backTextView.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        homeIcon.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)); finish() }
        homeTextView.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)); finish() }
        shopIcon.setOnClickListener { startActivity(Intent(this, CategoryListActivity::class.java)); finish() }
        shopTextView.setOnClickListener { startActivity(Intent(this, CategoryListActivity::class.java)); finish() }
        settingIcon.setOnClickListener { startActivity(Intent(this, SettingActivity::class.java)); finish() }
        settingTextView.setOnClickListener { startActivity(Intent(this, SettingActivity::class.java)); finish() }
        cartIconImageView.setOnClickListener { startActivity(Intent(this, CartActivity::class.java)) }
    }
}