package com.example.arhomedecorationapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Base64
import android.util.Log

class CategoryListActivity : AppCompatActivity() {

    private lateinit var categoryContainer: LinearLayout
    private lateinit var categoriesRef: DatabaseReference
    private lateinit var productsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_list)

        // Initialize your category layout and Firebase reference
        categoryContainer = findViewById(R.id.categoryContainer)
        categoriesRef = FirebaseDatabase.getInstance().getReference("category")
        productsRef = FirebaseDatabase.getInstance().getReference("product")

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

        val navigateHeaderTextView: TextView = findViewById(R.id.logoTitleText)
        navigateHeaderTextView.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        footerActivity()

        // Fetch categories from Firebase
        fetchCategoriesFromDatabase()
    }

    private fun fetchCategoriesFromDatabase() {
        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryContainer.removeAllViews() // Clear existing views to prevent duplication

                // Iterate through each category snapshot
                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.child("categoryName").getValue(String::class.java)

                    // If categoryName exists, create and display category view
                    categoryName?.let {
                        val categoryView = createCategoryView(it)

                        // Add category view to the main category container
                        categoryContainer.addView(categoryView)

                        // Fetch and display products associated with this specific category
                        val productContainer = categoryView.findViewById<LinearLayout>(R.id.productContainer)
                        fetchProductsForCategory(productContainer, it)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CategoryListActivity", "Database error: ${error.message}")
            }
        })
    }

    // Method to fetch and display products for a specific category
    private fun fetchProductsForCategory(productContainer: LinearLayout, categoryName: String) {
        productsRef.orderByChild("productCategory").equalTo(categoryName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(productSnapshot: DataSnapshot) {
                    productContainer.removeAllViews() // Clear existing product views for this category

                    // Iterate through each product and add it to the product container
                    for (product in productSnapshot.children) {
                        // Use createProductView to generate the product view
                        val productView = createProductView(product)

                        // Add the product view to the product container within this category
                        productContainer.addView(productView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CategoryListActivity", "Database error: ${error.message}")
                }
            })
    }

    // Method to create a category view
    private fun createCategoryView(categoryName: String): LinearLayout {
        val inflater = LayoutInflater.from(this)
        val categoryBox = inflater.inflate(R.layout.category_list_category_box, categoryContainer, false) as LinearLayout

        // Set category name in the view
        val categoryNameText: TextView = categoryBox.findViewById(R.id.categoryNameText)
        categoryNameText.text = categoryName

        // Setup "View More" click listener
        val viewMoreTextView: TextView = categoryBox.findViewById(R.id.viewMoreTextView)
        viewMoreTextView.setOnClickListener {
            val intent = Intent(this, CategoryDetailsActivity::class.java)
            intent.putExtra("categoryName", categoryName)
            startActivity(intent)
        }

        // Add a spacer (15dp) after each category box
            val spacer = View(this@CategoryListActivity)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.space_15dp) // Define the space dimension in dp
            )
            spacer.layoutParams = params
            categoryContainer.addView(spacer)

        return categoryBox
    }

    private fun createProductView(productSnapshot: DataSnapshot): LinearLayout {
        val inflater = LayoutInflater.from(this)
        val productBox = inflater.inflate(R.layout.category_list_product_box, null, false) as LinearLayout

        // Find views in the product box
        val productImage: ImageView = productBox.findViewById(R.id.productImage)
        val productNameTextView: TextView = productBox.findViewById(R.id.productDescription) // Assuming this is used for product name
        val productPrice: TextView = productBox.findViewById(R.id.productPrice)

        // Set the product name
        val productName = productSnapshot.child("productName").getValue(String::class.java) ?: "Unknown Product"
        productNameTextView.text = productName

        // Retrieve the first image in base64 and decode it
        val imageBase64 = productSnapshot.child("productImages").child("0").getValue(String::class.java)
        if (imageBase64 != null) {
            val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            productImage.setImageBitmap(decodedBitmap)
        } else {
            // Set a placeholder image if no image is available
            productImage.setImageResource(R.drawable.product1_image)
        }

        // Find the lowest price in productSpecs
        val productSpecs = productSnapshot.child("productSpecs")
        var lowestPrice: Double? = null

        for (specSnapshot in productSpecs.children) {
            val price = specSnapshot.child("productPrice").getValue(Double::class.java)
            if (price != null) {
                if (lowestPrice == null || price < lowestPrice) {
                    lowestPrice = price
                }
            }
        }

        // Display the lowest price with two decimal points
        productPrice.text = if (lowestPrice != null) {
            String.format("RM %.2f", lowestPrice)
        } else {
            "Price N/A"
        }

        // Retrieve the productId
        val productId = productSnapshot.child("id").getValue(String::class.java) ?: ""

        // Set a click listener to navigate to the ProductDetailsActivity with the productId
        productBox.setOnClickListener {
            val intent = Intent(this, ProductDetailsActivity::class.java)
            intent.putExtra("productId", productId) // Pass the productId as an extra
            startActivity(intent)
        }

        // Set space between product elements (space_8dp)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, resources.getDimensionPixelSize(R.dimen.space_10dp), 0)  // Add margin to the right

        productBox.layoutParams = params

        return productBox
    }

    private fun footerActivity() {
        val homeIcon: ImageView = findViewById(R.id.homeIcon)
        val homeTextView: TextView = findViewById(R.id.homeTextView)
        val shopIcon: ImageView = findViewById(R.id.shopIcon)
        val shopTextView: TextView = findViewById(R.id.shopTextView)
        val settingIcon: ImageView = findViewById(R.id.settingIcon)
        val settingTextView: TextView = findViewById(R.id.settingTextView)
        val cartIconImageView: ImageView = findViewById(R.id.cartIcon)

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

}

// Define CategoryList_Product class
data class CategoryList_Product(
    val description: String,
    val price: Double,
    val imageResource: Int
)

// Define CategoryList_Category class
data class CategoryList_Category(
    val name: String,
    val products: List<CategoryList_Product>
)
