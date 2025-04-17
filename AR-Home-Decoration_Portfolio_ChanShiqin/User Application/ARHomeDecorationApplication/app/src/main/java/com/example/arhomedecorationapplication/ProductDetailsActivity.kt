package com.example.arhomedecorationapplication

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.*

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var productsRef: DatabaseReference
    private var productId: String? = null
    private lateinit var productTitleBox: RelativeLayout
    private lateinit var addToCartButton: Button
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var colorChipGroup: ChipGroup
    private lateinit var quantityEditText: EditText
    private lateinit var decrementButton: TextView
    private lateinit var incrementButton: TextView
    private var quantity = 1

    private lateinit var database: DatabaseReference
    private var userId: String? = null
    private lateinit var userFoldersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        productsRef = FirebaseDatabase.getInstance().getReference("product")
        productTitleBox = findViewById(R.id.productTitleBox)
        productId = intent.getStringExtra("productId")

        footerActivity()

        fetchDataFromDatabase()

        addToCartButton = findViewById(R.id.addToCartButton)
        addToCartButton.setOnClickListener {
            showBottomSheetDialog()
        }

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("user_id", null)

        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        database = FirebaseDatabase.getInstance().getReference("user/$userId")
        userFoldersRef = FirebaseDatabase.getInstance().getReference("userfolder")

        val addToFileButton: Button = findViewById(R.id.addToFileButton)
        addToFileButton.setOnClickListener {
            fetchFoldersFromDatabase()
        }
    }

    private fun fetchFoldersFromDatabase() {
        userFoldersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val folders = mutableListOf<Pair<String, String>>()

                if (snapshot.exists()) {
                    for (folderSnapshot in snapshot.children) {
                        val folderId = folderSnapshot.child("folderId").getValue(String::class.java)
                        val folderName = folderSnapshot.child("folderName").getValue(String::class.java)
                        val folderUsers = folderSnapshot.child("folderUsers").children

                        var isUserFolder = false
                        for (user in folderUsers) {
                            val userIdInFolder = user.child("userId").getValue(String::class.java)
                            if (userIdInFolder == userId) {
                                isUserFolder = true
                                break
                            }
                        }

                        if (folderId != null && folderName != null && isUserFolder) {
                            folders.add(Pair(folderId, folderName))
                        }
                    }
                }

                // Pass folders to dialog, even if empty
                showFolderSelectionDialog(folders)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchFolders", "Failed to fetch folders: ${error.message}")
                Toast.makeText(this@ProductDetailsActivity, "Failed to fetch folders", Toast.LENGTH_SHORT).show()
            }
        })
    }

//    private fun showFolderSelectionDialog(folders: List<Pair<String, String>>) {
//        val folderNames = folders.map { it.second } // Extract folder names
//
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Select or Create Folder")
//
//        if (folderNames.isEmpty()) {
//            builder.setMessage("No folders available. You can create a new folder.")
//        } else {
//            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, folderNames)
//            builder.setAdapter(adapter) { _, which ->
//                saveProductToFolder(folders[which].first, false)
//            }
//        }
//
//        builder.setNegativeButton("Create New Folder") { _, _ ->
//            showCreateNewFolderDialog()
//        }
//
//        builder.setPositiveButton("Cancel") { dialog, _ -> dialog.dismiss() }
//        builder.show()
//    }

//    private fun showFolderSelectionDialog(folders: List<Pair<String, String>>) {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_create_favourite_folder, null)
//        val folderListView = dialogView.findViewById<ListView>(R.id.folderListView)
//        val btnCreateFolder = dialogView.findViewById<Button>(R.id.btnCreateFolder)
//        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
//
//        val folderNames = folders.map { it.second }
//        val adapter = ArrayAdapter(this, R.layout.list_item_folder, R.id.folderItemText, folderNames)
////        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, folderNames)
//        folderListView.adapter = adapter
//
//        val alertDialog = AlertDialog.Builder(this)
//            .setView(dialogView)
//            .create()
//
//        folderListView.setOnItemClickListener { _, _, position, _ ->
//            val selectedFolderId = folders[position].first
//            saveProductToFolder(selectedFolderId, false)
//            alertDialog.dismiss()
//        }
//
//        btnCreateFolder.setOnClickListener {
//            alertDialog.dismiss()
//            showCreateNewFolderDialog()
//        }
//
//        btnCancel.setOnClickListener {
//            alertDialog.dismiss()
//        }
//
//        alertDialog.show()
//    }

    private fun showFolderSelectionDialog(folders: List<Pair<String, String>>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_favourite_folder, null)
        val folderListView = dialogView.findViewById<ListView>(R.id.folderListView)
        val btnCreateFolder = dialogView.findViewById<Button>(R.id.btnCreateFolder)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        // Setup the folder list
        val folderNames = folders.map { it.second }
        val adapter = ArrayAdapter(this, R.layout.list_item_folder, R.id.folderItemText, folderNames)
        folderListView.adapter = adapter

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        folderListView.setOnItemClickListener { _, _, position, _ ->
            val selectedFolderId = folders[position].first
            saveProductToFolder(selectedFolderId, false)
            alertDialog.dismiss()
        }

        btnCreateFolder.setOnClickListener {
            alertDialog.dismiss()
            showCreateNewFolderDialog()
        }

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }



//    private fun showCreateNewFolderDialog() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Create New Folder")
//        val input = android.widget.EditText(this)
//        input.hint = "Enter folder name"
//        builder.setView(input)
//
//        builder.setPositiveButton("Create") { _, _ ->
//            val newFolderName = input.text.toString()
//            if (newFolderName.isNotEmpty()) {
//                createNewFolder(newFolderName)
//            } else {
//                Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
//        builder.show()
//    }

    private fun showCreateNewFolderDialog() {
        // Inflate the new dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_new_folder, null)

        // Initialize dialog elements
        val folderNameEditText = dialogView.findViewById<EditText>(R.id.folderNameEditText)
        val errorMessageText = dialogView.findViewById<TextView>(R.id.errorMessageText)
        val createFolderButton = dialogView.findViewById<Button>(R.id.btnCreateFolder)
        val cancelButton = dialogView.findViewById<Button>(R.id.btnCancelFolderCreation)

        // Create the dialog
        val folderDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Create button click listener
        createFolderButton.setOnClickListener {
            val folderName = folderNameEditText.text.toString().trim()

            if (folderName.isEmpty()) {
                // Show error if folder name is empty
                errorMessageText.visibility = View.VISIBLE
            } else {
                // Hide error message and create folder
                errorMessageText.visibility = View.GONE
                createNewFolder(folderName) // Call your function to create the folder
                folderDialog.dismiss() // Dismiss the dialog after folder creation
            }
        }

        // Cancel button click listener
        cancelButton.setOnClickListener {
            folderDialog.dismiss() // Simply dismiss the dialog
        }

        // Show the dialog
        folderDialog.show()
    }


    private fun createNewFolder(folderName: String) {
        // Create a new folder and save it in the `userfolder` table
        val newFolderId = "F${System.currentTimeMillis()}"
        val newFolder = mapOf(
            "folderId" to newFolderId,
            "folderName" to folderName,
            "folderUsers" to mapOf("0" to mapOf("userId" to userId, "userEmail" to "user@example.com")),
            "folderItems" to mutableListOf<Map<String, String>>()
        )

        val folderRef = userFoldersRef.child(newFolderId)
        folderRef.setValue(newFolder).addOnSuccessListener {
            Toast.makeText(this@ProductDetailsActivity, "Folder created", Toast.LENGTH_SHORT).show()
            // After folder creation, save the product to this folder
            saveProductToFolder(newFolderId, true) // True means it's a new folder, so add folder ID to user's folder list
        }.addOnFailureListener {
            Toast.makeText(this@ProductDetailsActivity, "Failed to create folder", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun saveProductToFolder(folderId: String, isNewFolder: Boolean) {
//        // Fetch product details by productId from the database
//        productsRef.orderByChild("id").equalTo(productId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    var productName: String? = null
//                    var productId: String? = null
//
//                    if (snapshot.exists()) {
//                        for (productSnapshot in snapshot.children) {
//                            productName = productSnapshot.child("productName").getValue(String::class.java)
//                            productId = productSnapshot.child("id").getValue(String::class.java)
//                        }
//
//                        if (productName != null && productId != null) {
//                            // Check if product already exists in the folder
//                            val folderRef = userFoldersRef.child(folderId).child("folderItems")
//                            folderRef.orderByChild("itemId").equalTo(productId).addListenerForSingleValueEvent(object : ValueEventListener {
//                                override fun onDataChange(folderSnapshot: DataSnapshot) {
//                                    if (folderSnapshot.exists()) {
//                                        // Product already exists in the folder
//                                        Toast.makeText(this@ProductDetailsActivity, "Product already exists in this folder", Toast.LENGTH_SHORT).show()
//                                    } else {
//                                        // Add the product to the folder
//                                        val newItem = mapOf(
//                                            "itemName" to productName,
//                                            "itemId" to productId
//                                        )
//
//                                        folderRef.push().setValue(newItem).addOnSuccessListener {
//                                            Toast.makeText(this@ProductDetailsActivity, "Product added to folder", Toast.LENGTH_SHORT).show()
//
//                                            // Only add folder ID to user's folder list if it's a new folder
//                                            if (isNewFolder) {
//                                                val userFolderRef = database.child("folder")
//                                                userFolderRef.push().setValue(mapOf("folderId" to folderId))
//                                            }
//                                        }.addOnFailureListener {
//                                            Toast.makeText(this@ProductDetailsActivity, "Failed to add product to folder", Toast.LENGTH_SHORT).show()
//                                        }
//                                    }
//                                }
//
//                                override fun onCancelled(error: DatabaseError) {
//                                    Toast.makeText(this@ProductDetailsActivity, "Failed to check if product exists in the folder", Toast.LENGTH_SHORT).show()
//                                }
//                            })
//                        }
//                    } else {
//                        Toast.makeText(this@ProductDetailsActivity, "No product found with this ID", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(this@ProductDetailsActivity, "Failed to fetch product data", Toast.LENGTH_SHORT).show()
//                }
//            })
//    }

    private fun saveProductToFolder(folderId: String, isNewFolder: Boolean) {
        // Fetch product details by productId from the database
        productsRef.orderByChild("id").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var productName: String? = null
                    var productId: String? = null

                    if (snapshot.exists()) {
                        for (productSnapshot in snapshot.children) {
                            productName = productSnapshot.child("productName").getValue(String::class.java)
                            productId = productSnapshot.child("id").getValue(String::class.java)
                        }

                        if (productName != null && productId != null) {
                            // Check if product already exists in the folder
                            val folderRef = userFoldersRef.child(folderId).child("folderItems")
                            folderRef.orderByChild("itemId").equalTo(productId).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(folderSnapshot: DataSnapshot) {
                                    if (folderSnapshot.exists()) {
                                        // Product already exists in the folder
                                        Toast.makeText(this@ProductDetailsActivity, "Product already exists in this folder", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Add the product to the folder with addedByUserId
                                        val newItem = mapOf(
                                            "itemName" to productName,
                                            "itemId" to productId,
                                            "addedByUserId" to userId // This is the user who added the product
                                        )

                                        folderRef.push().setValue(newItem).addOnSuccessListener {
                                            Toast.makeText(this@ProductDetailsActivity, "Product added to folder", Toast.LENGTH_SHORT).show()

                                            // Only add folder ID to user's folder list if it's a new folder
                                            if (isNewFolder) {
                                                val userFolderRef = database.child("folder")
                                                userFolderRef.push().setValue(mapOf("folderId" to folderId))
                                            }
                                        }.addOnFailureListener {
                                            Toast.makeText(this@ProductDetailsActivity, "Failed to add product to folder", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@ProductDetailsActivity, "Failed to check if product exists in the folder", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    } else {
                        Toast.makeText(this@ProductDetailsActivity, "No product found with this ID", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProductDetailsActivity, "Failed to fetch product data", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun fetchDataFromDatabase() {
        Log.d("ProductDetails", "Product ID: $productId")

        if (productId == null) {
            findViewById<TextView>(R.id.productTitleTextView).text = "Product ID is missing"
            return
        }

        productsRef.orderByChild("id").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productTitleTextView: TextView = findViewById(R.id.productTitleTextView)
                    val productPriceTextView: TextView = findViewById(R.id.productPriceTextView)
                    val productStockTextView: TextView = findViewById(R.id.stockCountTextView)
                    val productBrandTextView: TextView = findViewById(R.id.brandTextView)
                    val productMaterialTextView: TextView = findViewById(R.id.materialTextView)
                    val productSizeTextView: TextView = findViewById(R.id.sizeTextView)
                    val productWarrantyTextView: TextView = findViewById(R.id.warrentyTextView)
                    val productDescriptionTextView: TextView = findViewById(R.id.descriptionTextView)
                    val arViewButton: ImageView = findViewById(R.id.arViewBotton)

                    if (snapshot.exists()) {
                        for (productSnapshot in snapshot.children) {
                            val productName = productSnapshot.child("productName").getValue(String::class.java)
                            val pricesList = mutableListOf<Double>()
                            var totalStock = 0
                            val productBrand = productSnapshot.child("productBrandName").getValue(String::class.java)
                            val productMaterial = productSnapshot.child("productMaterial").getValue(String::class.java)
                            val productSizeWidth = productSnapshot.child("productSizeWidth").getValue(String::class.java)
                            val productSizeHeight = productSnapshot.child("productSizeHeight").getValue(String::class.java)
                            val productSizeDepth = productSnapshot.child("productSizeDepth").getValue(String::class.java)
                            val productDescription = productSnapshot.child("productDescription").getValue(String::class.java)

                            val hasWarranty = productSnapshot.child("hasWarranty").getValue(String::class.java)
                            val warrantyDurationValue = productSnapshot.child("warrantyDurationValue").getValue(String::class.java)
                            val warrantyDurationUnit = productSnapshot.child("warrantyDurationUnit").getValue(String::class.java)
                            val productAR = productSnapshot.child("productAR").getValue(String::class.java)

                            val productSpecsSnapshot = productSnapshot.child("productSpecs")
                            for (specSnapshot in productSpecsSnapshot.children) {
                                val productPrice = specSnapshot.child("productPrice").getValue(Double::class.java)
                                val productStock = specSnapshot.child("productStock").getValue(Int::class.java)

                                if (productPrice != null) {
                                    pricesList.add(productPrice)
                                }
                                if (productStock != null) {
                                    totalStock += productStock
                                }
                            }

                            val productImages = mutableListOf<String>()

                            val productImagesSnapshot = productSnapshot.child("productImages")
                            for (imageSnapshot in productImagesSnapshot.children) {
                                val imageUrl = imageSnapshot.getValue(String::class.java)
                                if (imageUrl != null) {
                                    productImages.add(imageUrl)
                                }
                            }

                            val viewPager = findViewById<ViewPager2>(R.id.productImageViewPager)
                            viewPager.adapter = ProductImagesAdapter(productImages)

                            val totalImages = productImages.size
                            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                override fun onPageSelected(position: Int) {
                                    super.onPageSelected(position)
                                }
                            })

                            productTitleTextView.text = productName ?: "Product name not found"
                            if (pricesList.isNotEmpty()) {
                                val minPrice = pricesList.minOrNull()
                                val maxPrice = pricesList.maxOrNull()

                                productPriceTextView.text = if (minPrice == maxPrice) {
                                    "RM %.2f".format(minPrice)
                                } else {
                                    "RM %.2f - %.2f".format(minPrice, maxPrice)
                                }
                            } else {
                                productPriceTextView.text = "Price not available"
                            }

                            productStockTextView.text = "$totalStock items left"
                            productBrandTextView.text = productBrand ?: "No Brand"
                            productMaterialTextView.text = productMaterial ?: "Didn't State"
                            productSizeTextView.text = "$productSizeWidth x $productSizeHeight x $productSizeDepth \n(Width x Height x Depth)"
                            productDescriptionTextView.text = productDescription

                            productWarrantyTextView.text = if (hasWarranty == "yes" && warrantyDurationValue != null && warrantyDurationUnit != null) {
                                "$warrantyDurationValue $warrantyDurationUnit"
                            } else {
                                "None"
                            }

                            // Toggle AR button visibility based on the presence of productAR
                            if (productAR.isNullOrEmpty()) {
                                arViewButton.visibility = View.GONE
                            } else {
                                arViewButton.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        productTitleTextView.text = "Product not found in database"
                        Log.d("ProductDetails", "No matching product found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProductDetails", "Database error: ${error.message}")
                    findViewById<TextView>(R.id.productTitleTextView).text = "Error fetching data"
                }
            })
    }

    private inner class ProductImagesAdapter(private val images: List<String>) :
        RecyclerView.Adapter<ProductImagesAdapter.ImageViewHolder>() {

        inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.productImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val image = images[position]

            if (image.startsWith("http")) {
                Glide.with(holder.imageView.context)
                    .load(image)
                    .into(holder.imageView)
            } else {
                val decodedString = Base64.decode(image, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                holder.imageView.setImageBitmap(decodedByte)
            }
        }

        override fun getItemCount(): Int = images.size
    }

    private fun showBottomSheetDialog() {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)

        // Initialize ChipGroup for selecting color
        colorChipGroup = view.findViewById(R.id.colorChipGroup)

        // Initialize the TextViews for stock and price
        val selectedStockTextView: TextView = view.findViewById(R.id.stockText)
        val selectedPriceTextView: TextView = view.findViewById(R.id.priceText)
        val productImageView: ImageView = view.findViewById(R.id.productImage)

        // Initialize quantity controls
        quantityEditText = view.findViewById(R.id.quantityEditText)
        decrementButton = view.findViewById(R.id.decrementButton)
        incrementButton = view.findViewById(R.id.incrementButton)

        // Set initial quantity
        var quantity = 1 // Set this to the desired initial value
        quantityEditText.setText(quantity.toString())

        // Handle decrement button click
        decrementButton.setOnClickListener {
            if (quantity > 1) {
                quantity-- // Decrement quantity
                quantityEditText.setText(quantity.toString()) // Update UI
            }
        }

        incrementButton.setOnClickListener {
            val stock = selectedStockTextView.text.toString().replace("Stock: ", "").toIntOrNull() ?: 0
            if (quantity < stock) {
                quantity++ // Increment quantity if it is less than stock
                quantityEditText.setText(quantity.toString()) // Update UI
            } else {
                Toast.makeText(this, "Cannot add more than available stock.", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize the BottomSheetDialog
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()

        // Load product specs and image after showing the dialog
        loadProductSpecs(selectedStockTextView, selectedPriceTextView)
        loadProductImage(productImageView)

        // Confirm button to add selected item to cart
        val confirmAddToCartButton: Button = view.findViewById(R.id.confirmAddToCartButton)

        confirmAddToCartButton.setOnClickListener {
//            val selectedChip = colorChipGroup.findViewById<Chip>(colorChipGroup.checkedChipId)
//            val selectedDesign = selectedChip?.text.toString()

            val selectedChipId = colorChipGroup.checkedChipId

            if (selectedChipId == View.NO_ID) {
                Toast.makeText(this, "Please select the design/color before adding to cart", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedChip = colorChipGroup.findViewById<Chip>(selectedChipId)
            val selectedDesign = selectedChip.text.toString()

            val quantity = quantityEditText.text.toString().toIntOrNull() ?: 1
            val stock = selectedStockTextView.text.toString().replace("Stock: ", "").toIntOrNull() ?: 0

            if (quantity > stock) {
                Toast.makeText(this, "Quantity exceeds available stock.", Toast.LENGTH_SHORT).show()
            } else if (productId != null && selectedDesign.isNotEmpty() && quantity > 0) {
                addToCart(productId!!, selectedDesign, quantity)
                bottomSheetDialog.dismiss() // Close the bottom sheet
            } else {
                Toast.makeText(this, "Please select a design and valid quantity.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun addToCart(productId: String, productDesign: String, productQuantity: Int) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)
        val cartRef = FirebaseDatabase.getInstance().getReference("user/$userId/cart")


        if (userId == null) {
            Toast.makeText(this, "User ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        // Query cart to check if the product with the same design already exists
        cartRef.orderByChild("productId").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var itemUpdated = false

                    for (cartItemSnapshot in snapshot.children) {
                        val existingDesign = cartItemSnapshot.child("productDesign").getValue(String::class.java)
                        val existingQuantity = cartItemSnapshot.child("productQuantity").getValue(Int::class.java) ?: 0

                        if (existingDesign == productDesign) {
                            // Update the quantity of the existing cart item
                            val newQuantity = existingQuantity + productQuantity
                            cartItemSnapshot.ref.child("productQuantity").setValue(newQuantity)
                                .addOnSuccessListener {
                                    Toast.makeText(this@ProductDetailsActivity, "Cart updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this@ProductDetailsActivity, "Failed to update cart: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            itemUpdated = true
                            break
                        }
                    }

                    if (!itemUpdated) {
                        // Add a new item if it doesn't exist
                        val newItem = mapOf(
                            "productId" to productId,
                            "productDesign" to productDesign,
                            "productQuantity" to productQuantity
                        )
                        cartRef.push().setValue(newItem)
                            .addOnSuccessListener {
                                Toast.makeText(this@ProductDetailsActivity, "Added to cart successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@ProductDetailsActivity, "Failed to add to cart: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProductDetailsActivity, "Failed to access cart: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadProductSpecs(selectedStockTextView: TextView, selectedPriceTextView: TextView) {
        if (productId == null) return

        // Query database to find the product with matching productId
        productsRef.orderByChild("id").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    colorChipGroup.removeAllViews()

                    if (snapshot.exists()) {
                        // Get the product node
                        for (productSnapshot in snapshot.children) {
                            val productSpecsSnapshot = productSnapshot.child("productSpecs")

                            // Variable to hold the first chip view for default selection
                            var firstChip: Chip? = null

                            for (specSnapshot in productSpecsSnapshot.children) {
                                val colorName = specSnapshot.child("productDesign").getValue(String::class.java)
                                val price = specSnapshot.child("productPrice").getValue(Double::class.java)
                                val stock = specSnapshot.child("productStock").getValue(Int::class.java)

                                if (colorName != null && price != null && stock != null) {
                                    val chip = Chip(this@ProductDetailsActivity)
                                    chip.text = colorName
                                    chip.isCheckable = true
                                    chip.id = View.generateViewId()

                                    // Disable chip if stock is 0
                                    chip.isClickable = stock > 0
                                    if (stock == 0) {
                                        chip.chipBackgroundColor = ColorStateList.valueOf(
                                            ContextCompat.getColor(this@ProductDetailsActivity, R.color.disabled_chip_color)
                                        )
                                    } else {
                                        chip.chipBackgroundColor = ColorStateList.valueOf(
                                            ContextCompat.getColor(this@ProductDetailsActivity, R.color.default_chip_color)
                                        )
                                    }

                                    chip.tag = Pair(price, stock)

                                    colorChipGroup.addView(chip)

                                    if (firstChip == null && stock > 0) {
                                        firstChip = chip
                                    }
                                }
                            }

                            // Handle selection logic for visual feedback and data update
                            colorChipGroup.setOnCheckedChangeListener { group, checkedId ->
                                for (i in 0 until group.childCount) {
                                    val chip = group.getChildAt(i) as Chip

                                    // Skip if not clickable (i.e., stock = 0)
                                    if (!chip.isClickable) continue

                                    if (chip.id == checkedId) {
                                        chip.chipBackgroundColor = ColorStateList.valueOf(
                                            ContextCompat.getColor(this@ProductDetailsActivity, R.color.selected_chip_color)
                                        )

                                        val (selectedPrice, selectedStock) = chip.tag as Pair<Double, Int>
                                        selectedStockTextView.text = "Stock: $selectedStock"
                                        selectedPriceTextView.text = "RM: %.2f".format(selectedPrice)
                                    } else {
                                        chip.chipBackgroundColor = ColorStateList.valueOf(
                                            ContextCompat.getColor(this@ProductDetailsActivity, R.color.default_chip_color)
                                        )
                                    }
                                }
                            }

                            // Default select the first available chip
                            if (firstChip != null) {
                                firstChip.isChecked = true
                                val (selectedPrice, selectedStock) = firstChip.tag as Pair<Double, Int>
                                selectedStockTextView.text = "Stock: $selectedStock"
                                selectedPriceTextView.text = "RM: %.2f".format(selectedPrice)
                            }
                        }
                    } else {
                        Log.e("ProductDetails", "No productSpecs found for productId: $productId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProductDetails", "Failed to load product specs: ${error.message}")
                }
            })
    }



//    private fun loadProductSpecs(selectedStockTextView: TextView, selectedPriceTextView: TextView) {
//        if (productId == null) return
//
//        // Query database to find the product with matching productId
//        productsRef.orderByChild("id").equalTo(productId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    colorChipGroup.removeAllViews()
//
//                    if (snapshot.exists()) {
//                        // Get the product node
//                        for (productSnapshot in snapshot.children) {
//                            // Access the productSpecs array
//                            val productSpecsSnapshot = productSnapshot.child("productSpecs")
//
//                            // Variable to hold the first chip view for default selection
//                            var firstChip: Chip? = null
//
//                            // Iterate over each spec to create color chips
//                            for (specSnapshot in productSpecsSnapshot.children) {
//                                val colorName = specSnapshot.child("productDesign").getValue(String::class.java)
//                                val price = specSnapshot.child("productPrice").getValue(Double::class.java)
//                                val stock = specSnapshot.child("productStock").getValue(Int::class.java)
//
//                                // Ensure price and stock are not null
//                                if (colorName != null && price != null && stock != null) {
//                                    val chip = Chip(this@ProductDetailsActivity)
//                                    chip.text = colorName
//                                    chip.isCheckable = true
////                                    chip.isClickable = true
////                                    chip.chipBackgroundColor = ColorStateList.valueOf(
////                                        ContextCompat.getColor(this@ProductDetailsActivity, R.color.default_chip_color)
////                                    )
//
//                                    // Disable chip if stock is 0
//                                    chip.isClickable = stock > 0
//                                    if (stock == 0) {
//                                        chip.chipBackgroundColor = ColorStateList.valueOf(
//                                            ContextCompat.getColor(this@ProductDetailsActivity, R.color.disabled_chip_color)
//                                        )
//                                    } else {
//                                        chip.chipBackgroundColor = ColorStateList.valueOf(
//                                            ContextCompat.getColor(this@ProductDetailsActivity, R.color.default_chip_color)
//                                        )
//                                    }
//
//                                    // Store price and stock in the chip's tag
//                                    chip.tag = Pair(price, stock)
//
//                                    // Set the chip click listener
//                                    chip.setOnClickListener {
//                                        val isSelected = chip.isChecked
//                                        val chipColor = if (isSelected) {
//                                            ContextCompat.getColor(this@ProductDetailsActivity, R.color.selected_chip_color)
//                                        } else {
//                                            ContextCompat.getColor(this@ProductDetailsActivity, R.color.default_chip_color)
//                                        }
//                                        chip.chipBackgroundColor = ColorStateList.valueOf(chipColor)
//
//                                        // Get the price and stock from the chip's tag
//                                        val (selectedPrice, selectedStock) = chip.tag as Pair<Double, Int>
//
//                                        selectedStockTextView.text = "Stock: $selectedStock"
//                                        selectedPriceTextView.text = "RM: %.2f".format(selectedPrice)
//                                    }
//
//                                    // Add the chip to the ChipGroup
//                                    colorChipGroup.addView(chip)
//
//                                    // Store the first chip for default selection
//                                    if (firstChip == null) {
//                                        firstChip = chip
//                                    }
//                                }
//                            }
//
//                            // Default select the first chip if available
//                            firstChip?.isChecked = true
//                            // Optionally, update the price and stock text with the first chip's data
//                            if (firstChip != null) {
//                                val (selectedPrice, selectedStock) = firstChip!!.tag as Pair<Double, Int>
//                                selectedStockTextView.text = "Stock: $selectedStock"
//                                selectedPriceTextView.text = "RM: %.2f".format(selectedPrice)
//                            }
//                        }
//                    } else {
//                        Log.e("ProductDetails", "No productSpecs found for productId: $productId")
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("ProductDetails", "Failed to load product specs: ${error.message}")
//                }
//            })
//    }

    private fun loadProductImage(productImageView: ImageView) {
        if (productId == null) return

        // Query database to find the product with matching productId
        productsRef.orderByChild("id").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Retrieve the first product matching the query
                        for (productSnapshot in snapshot.children) {
                            // Access the first image from "productImages" array
                            val imageBase64 = productSnapshot.child("productImages").child("0").getValue(String::class.java)
                            if (imageBase64 != null) {
                                try {
                                    // Decode Base64 to bytes and create a Bitmap
                                    val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
                                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    // Set the Bitmap to the ImageView
                                    productImageView.setImageBitmap(decodedBitmap)
                                } catch (e: IllegalArgumentException) {
                                    Log.e("ProductDetails", "Failed to decode image: ${e.message}")
                                }
                            } else {
                                Log.e("ProductDetails", "No image data found in productImages for productId: $productId")
                            }
                            break // Exit after the first matching product
                        }
                    } else {
                        Log.e("ProductDetails", "No product found for productId: $productId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProductDetails", "Failed to load product image: ${error.message}")
                }
            })
    }

    private fun footerActivity() {
        val homeIcon: ImageView = findViewById(R.id.homeIcon)
        val homeTextView: TextView = findViewById(R.id.homeTextView)
        val shopIcon: ImageView = findViewById(R.id.shopIcon)
        val shopTextView: TextView = findViewById(R.id.shopTextView)
        val settingIcon: ImageView = findViewById(R.id.settingIcon)
        val settingTextView: TextView = findViewById(R.id.settingTextView)
        val navigateHeaderTextView: TextView = findViewById(R.id.logoTitleText)
        val navigateBackTextView: TextView = findViewById(R.id.backTextView)
        val cartIconImageView: ImageView = findViewById(R.id.cartIcon)
        val arViewBotton: ImageView = findViewById(R.id.arViewBotton)

        navigateHeaderTextView.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        navigateBackTextView.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
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
        arViewBotton.setOnClickListener {
            val intent = Intent(this, ArViewActivity::class.java)
            intent.putExtra("productId", productId)
            startActivity(intent)
        }
    }

}