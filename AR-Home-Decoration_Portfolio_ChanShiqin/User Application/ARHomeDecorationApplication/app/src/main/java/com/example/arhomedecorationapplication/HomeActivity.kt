package com.example.arhomedecorationapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineStart
import kotlin.io.encoding.Base64

class HomeActivity : AppCompatActivity() {

    private lateinit var categoryLayout: LinearLayout
    private lateinit var categoriesRef: DatabaseReference

    private lateinit var hotItemsRecyclerView: LinearLayout
//    private lateinit var hotItemsAdapter: HotItemsAdapter
    private lateinit var ordersRef: DatabaseReference
    private lateinit var productsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize your category layout and Firebase reference
        categoryLayout = findViewById(R.id.categoryLayout)
        categoriesRef = FirebaseDatabase.getInstance().getReference("category")

        // Find the button by its ID
        val navigateTextView: TextView = findViewById(R.id.viewMoreCategoryTextView)

        // Set an OnClickListener on the button
        navigateTextView.setOnClickListener {
            // Create an intent to navigate to SecondActivity
            val intent = Intent(this, CategoryListActivity::class.java)
            startActivity(intent) // Start the SecondActivity
            finish()
        }

        val navigateHeaderTextView: TextView = findViewById(R.id.logoTitleText)
        navigateHeaderTextView.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        footerActivity()

        // Fetch categories from the database
        fetchCategoriesFromDatabase()

        // Initialize Firebase references
        ordersRef = FirebaseDatabase.getInstance().getReference("order")
        productsRef = FirebaseDatabase.getInstance().getReference("product")

        hotItemsRecyclerView = findViewById(R.id.hotItemsRecyclerView)
        fetchTopHotItems()
    }

    private fun fetchTopHotItems() {
        // Step 1: Fetch orders to get the most ordered products
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Map to store the product ID and its order count
                val productOrderCount = mutableMapOf<String, Int>()

                // Loop through orders and count the product orders
                for (orderSnapshot in snapshot.children) {
                    val items = orderSnapshot.child("items")
                    for (itemSnapshot in items.children) {
                        val productId = itemSnapshot.child("productId").getValue(String::class.java)
                        val productQuantity = itemSnapshot.child("productQuantity").getValue(Int::class.java) ?: 0
                        if (productId != null) {
                            productOrderCount[productId] = productOrderCount.getOrDefault(productId, 0) + productQuantity
                        }
                    }
                }

                // Step 2: Sort the products by the number of orders and get the top 6
                val sortedProducts = productOrderCount.toList()
                    .sortedByDescending { it.second } // Sort by quantity ordered
                    .take(6) // Get top 6 products

                // Step 3: Fetch the details of the top 6 products
                fetchProductDetails(sortedProducts.map { it.first })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Failed to fetch orders: ${error.message}")
            }
        })
    }

    private fun fetchProductDetails(productIds: List<String>) {
        // Step 4: Fetch product details for the top 6 products
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hotItemsRecyclerView.removeAllViews() // Clear existing views

                var count = 0
                for (productSnapshot in snapshot.children) {
                    val productId = productSnapshot.child("id").getValue(String::class.java)
                    if (productId != null && productIds.contains(productId) && count < 6) {
                        val productName = productSnapshot.child("productName").getValue(String::class.java)
                        val productImages = productSnapshot.child("productImages").children
                        val productSpecs = productSnapshot.child("productSpecs").children

                        val productImageBase64 = productImages.firstOrNull()?.getValue(String::class.java)
                        val productPrice = productSpecs.minByOrNull { it.child("productPrice").getValue(Int::class.java) ?: Int.MAX_VALUE }
                            ?.child("productPrice")?.getValue(Int::class.java) ?: 0

                        // Step 5: Create and add the product box to the RecyclerView
                        val productBoxView = LayoutInflater.from(this@HomeActivity).inflate(R.layout.product_box, null)

                        // Set margin bottom for the product box
                        // Set up LayoutParams for margin bottom
                        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        layoutParams.setMargins(dpToPx(0), 0, dpToPx(0), dpToPx(10)) // Adjusting marginBottom here

                        productBoxView.layoutParams = layoutParams

                        val productImageView: ImageView = productBoxView.findViewById(R.id.productImage)
                        val productTitleView: TextView = productBoxView.findViewById(R.id.productTitle)
                        val productPriceView: TextView = productBoxView.findViewById(R.id.productPrice)

                        // Decode the base64 image and set it
                        productImageBase64?.let {
                            val bitmap = decodeBase64ToBitmap(it)
                            productImageView.setImageBitmap(bitmap)
                        }

                        productTitleView.text = productName ?: "Unknown Product"
                        productPriceView.text = "RM $productPrice"

                        // Add the product box to the RecyclerView
                        hotItemsRecyclerView.addView(productBoxView)
                        count++

                        // Set OnClickListener to navigate to ProductDetailsActivity with productId
                        productBoxView.setOnClickListener {
                            val intent = Intent(this@HomeActivity, ProductDetailsActivity::class.java)
                            intent.putExtra("productId", productId)  // Pass the productId
                            startActivity(intent) // Start the ProductDetailsActivity
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeActivity", "Failed to fetch products: ${error.message}")
            }
        })
    }


    private fun fetchCategoriesFromDatabase() {
        categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryLayout.removeAllViews() // Remove any existing views

                // Loop through the categories and add them to the layout
                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.child("categoryName").getValue(String::class.java)
                    val categoryIcon = categorySnapshot.child("categoryIcon").getValue(String::class.java)

                    if (categoryName != null && categoryIcon != null) {
                        val categoryView = createCategoryView(categoryName, categoryIcon)
                        categoryLayout.addView(categoryView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun createCategoryView(categoryName: String, categoryIcon: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 1f // Equal width for each category
                marginStart = dpToPx(10)
                marginEnd = dpToPx(10)
                width = dpToPx(60)
            }

            val imageView = ImageView(this@HomeActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(40), dpToPx(40))
                setImageBitmap(decodeBase64ToBitmap(categoryIcon))
            }

            val textView = TextView(this@HomeActivity).apply {
                width = dpToPx(180)
                text = categoryName
                setTextColor(resources.getColor(android.R.color.black))
                textSize = 11f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, dpToPx(10), 0, 0)
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            // Add click listener to the entire category view
            setOnClickListener {
                // Start the CategoryDetailsActivity with the category name as extra
                val intent = Intent(this@HomeActivity, CategoryDetailsActivity::class.java)
                intent.putExtra("categoryName", categoryName)
                startActivity(intent)
            }

            addView(imageView)
            addView(textView)
        }
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

data class Product(
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImage: String
)
