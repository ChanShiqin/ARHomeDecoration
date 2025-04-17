package com.example.arhomedecorationapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*

class ShareableFolderActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var productRef: DatabaseReference
    private lateinit var userReference: DatabaseReference
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shareable_folder)
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

        // Populate folders and their products
        populateFolders()
    }

    private fun populateFolders() {
        // Fetch the folder IDs from user folder reference
        val userFolderRef = userReference.child("folder")

        userFolderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Iterate through the folder references
                    for (folderSnapshot in snapshot.children) {
                        val folderId = folderSnapshot.child("folderId").getValue(String::class.java)
                        if (folderId != null) {
                            // Fetch the folder details from 'userfolder' node using folderId
                            val userFolder = database.child("userfolder").child(folderId)

                            userFolder.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(folderSnapshot: DataSnapshot) {
                                    if (folderSnapshot.exists()) {
                                        val folderName = folderSnapshot.child("folderName").getValue(String::class.java) ?: "Unknown Folder"
                                        val folderItems = folderSnapshot.child("folderItems")

                                        // Inflate and add folder layout
                                        val folderContainer: LinearLayout = findViewById(R.id.folderContainer)
                                        val folderView = LayoutInflater.from(this@ShareableFolderActivity).inflate(R.layout.shareable_folder, folderContainer, false)

                                        val fileNameTextView: TextView = folderView.findViewById(R.id.fileNameText)
                                        val viewMoreTextView: TextView = folderView.findViewById(R.id.viewMoreTextView)
                                        val productContainer: LinearLayout = folderView.findViewById(R.id.productContainer)

                                        fileNameTextView.text = folderName

                                        // Handle "View More" click
                                        viewMoreTextView.setOnClickListener {
                                            val intent = Intent(this@ShareableFolderActivity, ShareableFolderDetailsActivity::class.java)
                                            intent.putExtra("FOLDER_ID", folderId) // Pass folderId to the next activity
                                            startActivity(intent)
                                        }

                                        // Limit to first 2 items in folderItems
                                        folderItems.children.take(2).forEach { itemSnapshot ->
                                            val itemId = itemSnapshot.child("itemId").getValue(String::class.java) ?: ""
                                            val itemName = itemSnapshot.child("itemName").getValue(String::class.java) ?: "Unknown Product"

                                            Log.d("ShareableFolderActivity", "Fetching product details for $itemName with ID: $itemId")

                                            // Query product details from 'product' table using itemId
                                            productRef.orderByChild("id").equalTo(itemId)
                                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                                    override fun onDataChange(productSnapshot: DataSnapshot) {
                                                        if (productSnapshot.exists()) {
                                                            val productData = productSnapshot.children.first()
                                                            val productName = productData.child("productName").getValue(String::class.java) ?: "Unknown Product"
                                                            val productImages = productData.child("productImages").children.map { it.getValue(String::class.java) ?: "" }
                                                            val productSpecs = productData.child("productSpecs").children

                                                            // Get the lowest price from productSpecs
                                                            val lowestPrice = productSpecs.map {
                                                                it.child("productPrice").getValue(Double::class.java) ?: Double.MAX_VALUE
                                                            }.minOrNull() ?: 0.0

                                                            // Get the first image from productImages
                                                            val productImage = productImages.getOrNull(0) ?: ""

                                                            // Inflate the product box and set the product details
                                                            val productBoxView = LayoutInflater.from(this@ShareableFolderActivity).inflate(R.layout.product_box, productContainer, false)

                                                            val productTitle: TextView = productBoxView.findViewById(R.id.productTitle)
                                                            val productPrice: TextView = productBoxView.findViewById(R.id.productPrice)
                                                            val productImageView: ImageView = productBoxView.findViewById(R.id.productImage)

                                                            productTitle.text = productName
                                                            productPrice.text = "RM $lowestPrice"

                                                            // Decode Base64 image and set to ImageView
                                                            if (productImage.isNotEmpty()) {
                                                                val bitmap = decodeBase64ToBitmap(productImage)
                                                                productImageView.setImageBitmap(bitmap)
                                                            } else {
                                                                Log.d("ShareableFolderActivity", "No image found for product: $productName")
                                                            }

                                                            // Add product box to the folder's product container
                                                            productContainer.addView(productBoxView)

                                                            // Set click listener for navigating to product page
                                                            productBoxView.setOnClickListener {
                                                                val intent = Intent(this@ShareableFolderActivity, ProductDetailsActivity::class.java)
                                                                intent.putExtra("productId", itemId)
                                                                startActivity(intent)
                                                            }

                                                            Log.d("ShareableFolderActivity", "Product box added for: $productName")
                                                        } else {
                                                            Log.d("ShareableFolderActivity", "No product found for ID: $itemId")
                                                        }
                                                    }

                                                    override fun onCancelled(error: DatabaseError) {
                                                        Toast.makeText(this@ShareableFolderActivity, "Failed to load product details", Toast.LENGTH_SHORT).show()
                                                        Log.e("ShareableFolderActivity", "Error fetching product details", error.toException())
                                                    }
                                                })
                                        }

                                        // Add folder view to folder container
                                        folderContainer.addView(folderView)
                                        Log.d("ShareableFolderActivity", "Folder added with name: $folderName")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@ShareableFolderActivity, "Failed to load folder details", Toast.LENGTH_SHORT).show()
                                    Log.e("ShareableFolderActivity", "Error fetching folder details", error.toException())
                                }
                            })
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShareableFolderActivity, "Failed to load folders", Toast.LENGTH_SHORT).show()
                Log.e("ShareableFolderActivity", "Error fetching folders", error.toException())
            }
        })
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

