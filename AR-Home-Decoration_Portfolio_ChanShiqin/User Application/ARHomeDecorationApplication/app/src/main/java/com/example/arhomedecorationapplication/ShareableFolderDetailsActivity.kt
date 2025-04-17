package com.example.arhomedecorationapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*

class ShareableFolderDetailsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var productRef: DatabaseReference
    private lateinit var folderId: String
    private lateinit var userReference: DatabaseReference
    private var userId: String? = null

    private var currentDialog: AlertDialog? = null

    private var isNavigatingBackToUserList = false

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
            showUserListDialog()  // This will show dialog_user_list.xml
        }

        // Set up the delete folder button
        val deleteFolderButton: ImageButton = findViewById(R.id.deleteFolderButton)
        deleteFolderButton.setOnClickListener {
            confirmDeleteFolder()
        }

    }

    private fun showUserListDialog() {
        // Dismiss any open dialog
        currentDialog?.dismiss()

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_list, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewUserList)
        val addUserButton = dialogView.findViewById<Button>(R.id.addUserToFolderButton)
        val manageIcon = dialogView.findViewById<ImageView>(R.id.managePersonIcon)

        addUserButton.setOnClickListener {
            showAddPersonDialog()
        }

        manageIcon.setOnClickListener {
            showManageUserDialog()
        }

        // You can set click listener on manageIcon or addUserButton here if needed

        recyclerView.layoutManager = LinearLayoutManager(this)

        val folderRef = database.child("userfolder").child(folderId)

        folderRef.child("folderUsers").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<View>()

                val ownerId = snapshot.child("0").child("userId").getValue(String::class.java)

                for (userSnap in snapshot.children) {
                    val uid = userSnap.child("userId").getValue(String::class.java)
                    val userKey = userSnap.key

                    if (uid != null) {
                        database.child("user").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val name = userSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                                val isOwner = userKey == "0"

                                val itemView = LayoutInflater.from(this@ShareableFolderDetailsActivity)
                                    .inflate(R.layout.item_user_list, recyclerView, false)

                                val nameText = itemView.findViewById<TextView>(R.id.userNameText)
                                val roleText = itemView.findViewById<TextView>(R.id.userRoleText)

                                nameText.text = name
                                roleText.visibility = if (isOwner) View.VISIBLE else View.GONE

                                // Add item view to list
                                userList.add(itemView)

                                // Once all user views are loaded, update RecyclerView
                                if (userList.size == snapshot.childrenCount.toInt()) {
                                    recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                                            return object : RecyclerView.ViewHolder(userList[viewType]) {}
                                        }

                                        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                                            // Already inflated and set, nothing more needed here
                                        }

                                        override fun getItemCount(): Int = userList.size

                                        override fun getItemViewType(position: Int): Int = position
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to load users.", Toast.LENGTH_SHORT).show()
            }
        })

//        val dialog = AlertDialog.Builder(this)
//            .setView(dialogView)
//            .setCancelable(true)
//            .create()
//
//        dialog.show()

        currentDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        currentDialog?.show()
    }


    private fun showManageUserDialog() {
        // Dismiss current dialog before opening new one
        currentDialog?.dismiss()

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_manage_user_list, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerViewManageUserList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val folderRef = database.child("userfolder").child(folderId)

        folderRef.child("folderUsers").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userList = mutableListOf<Pair<String, String>>() // Pair<userKey, userId>
                val ownerId = snapshot.child("0").child("userId").getValue(String::class.java)
                val currentUserIsOwner = ownerId == userId

                for (userSnap in snapshot.children) {
                    val uid = userSnap.child("userId").getValue(String::class.java)
                    val userKey = userSnap.key
                    if (uid != null && userKey != null) {
                        userList.add(Pair(userKey, uid))
                    }
                }

                val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_user_list, parent, false)
                        return object : RecyclerView.ViewHolder(view) {}
                    }

                    override fun getItemCount(): Int = userList.size

                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        val userKey = userList[position].first
                        val uid = userList[position].second

                        val nameText = holder.itemView.findViewById<TextView>(R.id.userNameText)
                        val roleText = holder.itemView.findViewById<TextView>(R.id.userRoleText)
                        val actionBtn = holder.itemView.findViewById<Button>(R.id.userActionButton)

                        database.child("user").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnap: DataSnapshot) {
                                val name = userSnap.child("name").getValue(String::class.java) ?: "Unknown"
                                nameText.text = name

                                val isOwner = userKey == "0"
                                val isCurrentUser = uid == userId

                                when {
                                    isOwner -> {
                                        roleText.visibility = View.VISIBLE
                                        actionBtn.visibility = View.GONE
                                    }
                                    currentUserIsOwner -> {
                                        roleText.visibility = View.GONE
                                        actionBtn.visibility = View.VISIBLE
                                        actionBtn.text = "Remove"
                                        actionBtn.setOnClickListener {
                                            removeUserFromFolder(uid)
                                        }
                                    }
                                    isCurrentUser -> {
                                        roleText.visibility = View.GONE
                                        actionBtn.visibility = View.VISIBLE
                                        actionBtn.text = "Quit"
                                        actionBtn.setOnClickListener {
                                            quitFolderForCurrentUser(uid)
                                        }
                                    }
                                    else -> {
                                        roleText.visibility = View.GONE
                                        actionBtn.visibility = View.GONE
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }

                recyclerView.adapter = adapter

                // 🔽 Setup dialog here after data is ready
                currentDialog = AlertDialog.Builder(this@ShareableFolderDetailsActivity)
                    .setView(dialogView)
                    .setCancelable(true)
                    .create()

//                currentDialog?.setOnDismissListener {
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        showUserListDialog() // 👈 Reopen user list dialog
//                    }, 300)
//                }
//
//                currentDialog?.setOnDismissListener {
//                    if (!isNavigatingBackToUserList) { // ✅ Prevent double show
//                        Handler(Looper.getMainLooper()).postDelayed({
//                            showUserListDialog()
//                        }, 300)
//                    }
//                }


                currentDialog?.show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to load users.", Toast.LENGTH_SHORT).show()
            }
        })

    }


    private fun removeUserFromFolder(uid: String) {
        val folderRef = database.child("userfolder").child(folderId).child("folderUsers")
        val userFolderRef = database.child("user").child(uid).child("folder")

        var folderRemoveDone = false
        var userRemoveDone = false

        fun checkAndShowDialog() {
            if (folderRemoveDone && userRemoveDone) {
                Toast.makeText(this@ShareableFolderDetailsActivity, "User removed from folder", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    showUserListDialog()
                }, 300)
            }
        }

        folderRef.orderByChild("userId").equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task<Void>>()
                    for (child in snapshot.children) {
                        val task = child.ref.removeValue()
                        tasks.add(task)
                    }

                    Tasks.whenAllComplete(tasks).addOnCompleteListener {
                        folderRemoveDone = true
                        checkAndShowDialog()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        userFolderRef.orderByChild("folderId").equalTo(folderId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task<Void>>()
                    for (child in snapshot.children) {
                        val task = child.ref.removeValue()
                        tasks.add(task)
                    }

                    Tasks.whenAllComplete(tasks).addOnCompleteListener {
                        userRemoveDone = true
                        checkAndShowDialog()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }


    private fun quitFolderForCurrentUser(uid: String) {
        val folderRef = database.child("userfolder").child(folderId).child("folderUsers")
        val userFolderRef = database.child("user").child(uid).child("folder")

        // Remove from userfolder > folderUsers
        folderRef.orderByChild("userId").equalTo(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    child.ref.removeValue()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        // Remove from user's folder list
        userFolderRef.orderByChild("folderId").equalTo(folderId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    child.ref.removeValue()
                }

                Toast.makeText(this@ShareableFolderDetailsActivity, "You have quit the folder", Toast.LENGTH_SHORT).show()

                // ✅ Restart ShareableFolderActivity freshly
                val intent = Intent(this@ShareableFolderDetailsActivity, ShareableFolderActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                finish() // Close current activity
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    private fun confirmDeleteFolder() {
        val folderRef = database.child("userfolder").child(folderId)

        // Check if current user is the owner (index 0 in folderUsers)
        folderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val folderUsers = snapshot.child("folderUsers")
                    val ownerId = folderUsers.child("0").child("userId").getValue(String::class.java)

                    if (ownerId == userId) {
                        // Current user is the owner, show confirmation dialog
                        val dialogView = LayoutInflater.from(this@ShareableFolderDetailsActivity)
                            .inflate(R.layout.dialog_custom_delete_folder, null)

                        val dialog = AlertDialog.Builder(this@ShareableFolderDetailsActivity)
                            .setView(dialogView)
                            .setCancelable(false)
                            .create()

                        val btnDelete: Button = dialogView.findViewById(R.id.btnDelete)
                        val btnCancel: Button = dialogView.findViewById(R.id.btnCancel)

                        btnDelete.setOnClickListener {
                            deleteFolder()
                            dialog.dismiss()
                        }

                        btnCancel.setOnClickListener {
                            dialog.dismiss()
                        }

                        dialog.show()
                    } else {
                        // Not the owner
                        Toast.makeText(
                            this@ShareableFolderDetailsActivity,
                            "Only the folder owner can delete this folder.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(this@ShareableFolderDetailsActivity, "Folder not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShareableFolderDetailsActivity, "Failed to check folder ownership.", Toast.LENGTH_SHORT).show()
            }
        })
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
        // Dismiss current dialog before opening new one
        currentDialog?.dismiss()

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_person, null)
        val emailEditText: EditText = dialogView.findViewById(R.id.emailEditText)
        val errorMessageText: TextView = dialogView.findViewById(R.id.errorMessageText)
        val addButton: Button = dialogView.findViewById(R.id.btnAddPerson)
        val cancelButton: Button = dialogView.findViewById(R.id.btnCancel)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Handle Add button click
        addButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorMessageText.visibility = View.GONE
                checkEmailAndAddUser(email, dialog, errorMessageText)
            } else {
                errorMessageText.visibility = View.VISIBLE
                errorMessageText.text = "Please enter a valid email"
            }
        }

        // Handle Cancel button click
        cancelButton.setOnClickListener {
            dialog.dismiss()
            // After cancel, reopen the user list dialog to show updated state
//            showUserListDialog()
        }

        // 🔄 When dialog is dismissed (cancel button, outside click, or back press), reopen user list
        dialog.setOnDismissListener {
            Handler(Looper.getMainLooper()).postDelayed({
                showUserListDialog()
            }, 300) // Optional smooth transition delay
        }

        dialog.show()
        currentDialog = dialog
    }


    private fun checkEmailAndAddUser(email: String, dialog: AlertDialog, errorMessageText: TextView) {
        val userRef = database.child("user").orderByChild("email").equalTo(email)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Email found, get userId and add folder to user
                    val userId = snapshot.children.first().key ?: return
                    val userEmail = snapshot.children.first().child("email").getValue(String::class.java) ?: ""
                    addFolderToUser(userId, userEmail)
                    dialog.dismiss() // Close the dialog after adding user
                    // 👉 After dismiss, show back the updated user list
                    Handler(Looper.getMainLooper()).postDelayed({
                        showUserListDialog()
                    }, 300) // Small delay ensures smooth transition
                } else {
                    // Email not found, show error message in dialog
                    errorMessageText.visibility = View.VISIBLE
                    errorMessageText.text = "Email not found, please register first."
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
                    // Folder found, check if the user is already in the folder
                    var userAlreadyAdded = false
                    for (folder in folderSnapshot.children) {
                        val folderUsersRef = folder.child("folderUsers")

                        // Check if the user already exists in the folder
                        for (userSnapshot in folderUsersRef.children) {
                            val existingUserEmail = userSnapshot.child("userEmail").getValue(String::class.java)
                            if (existingUserEmail == userEmail) {
                                userAlreadyAdded = true
                                break
                            }
                        }

                        if (!userAlreadyAdded) {
                            // Add new user to folderUsers
                            val newUser = hashMapOf(
                                "userEmail" to userEmail,
                                "userId" to userId
                            )
                            folderUsersRef.ref.push().setValue(newUser)

                            // Add folderId to the user's folder list
                            val userFolderRef = database.child("user").child(userId).child("folder")
                            userFolderRef.push().setValue(mapOf("folderId" to folderId))

                            Toast.makeText(this@ShareableFolderDetailsActivity, "Folder shared with $userEmail", Toast.LENGTH_SHORT).show()
                        } else {
                            // User is already in the folder
                            Toast.makeText(this@ShareableFolderDetailsActivity, "User is already in this folder.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@ShareableFolderDetailsActivity, "Folder not found", Toast.LENGTH_SHORT).show()
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
                val productContainer: LinearLayout = findViewById(R.id.productContainer)
                productContainer.removeAllViews()

                if (snapshot.exists()) {
                    for (folderSnapshot in snapshot.children) {
                        val folderIdFromDb = folderSnapshot.child("folderId").getValue(String::class.java) ?: ""
                        val folderName = folderSnapshot.child("folderName").getValue(String::class.java) ?: ""
                        val folderItems = folderSnapshot.child("folderItems")

                        // Check if the folderId matches the current folderId
                        if (folderIdFromDb == folderId) {
                            val folderNameTextView: TextView = findViewById(R.id.folderName)
                            folderNameTextView.text = folderName

                            // Loop through the folderItems and display them
                            for (itemSnapshot in folderItems.children) {
                                val itemName = itemSnapshot.child("itemName").getValue(String::class.java) ?: "Unknown Product"
                                val itemId = itemSnapshot.child("itemId").getValue(String::class.java) ?: ""
                                val addedByUserId = itemSnapshot.child("addedByUserId").getValue(String::class.java) ?: ""

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

                                            // Add delete button functionality (only for the user who added the product)
                                            val deleteButton: ImageView = productBoxView.findViewById(R.id.deleteButton)
                                            if (addedByUserId == userId) {
                                                deleteButton.setVisibility(View.VISIBLE)
                                                deleteButton.setOnClickListener {
                                                    deleteProductFromFolder(itemId)
                                                }
                                            } else {
                                                deleteButton.setVisibility(View.GONE)  // Hide delete button if not the user who added the product
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

//        backTextView.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        backTextView.setOnClickListener {
            val intent = Intent(this, ShareableFolderActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Clears the activity stack and restarts the activity
            startActivity(intent)
            finish()
        }

        homeIcon.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)); finish() }
        homeTextView.setOnClickListener { startActivity(Intent(this, HomeActivity::class.java)); finish() }
        shopIcon.setOnClickListener { startActivity(Intent(this, CategoryListActivity::class.java)); finish() }
        shopTextView.setOnClickListener { startActivity(Intent(this, CategoryListActivity::class.java)); finish() }
        settingIcon.setOnClickListener { startActivity(Intent(this, SettingActivity::class.java)); finish() }
        settingTextView.setOnClickListener { startActivity(Intent(this, SettingActivity::class.java)); finish() }
        cartIconImageView.setOnClickListener { startActivity(Intent(this, CartActivity::class.java)) }
    }
}

data class FolderUser(
    val userName: String,
    val isOwner: Boolean
)
