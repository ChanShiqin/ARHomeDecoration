package com.example.arhomedecorationapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
//import kotlin.io.encoding.Base64
import android.util.Base64
import android.util.DisplayMetrics
import android.widget.GridLayout
import androidx.recyclerview.widget.RecyclerView


class CategoryDetailsActivity : AppCompatActivity() {

    private lateinit var categoryName: String
    private lateinit var productContainer: LinearLayout
    private lateinit var productsRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_details)

        // Properly initialize views after setContentView
        productContainer = findViewById(R.id.productContainer)

        productsRef = FirebaseDatabase.getInstance().getReference("product")
//        productContainer = findViewById(R.id.productContainer)

        // Get the categoryName passed from the intent
        categoryName = intent.getStringExtra("categoryName") ?: ""

        // Display category name
        val categoryNameTextView: TextView = findViewById(R.id.categoryDetailsCategoryName)
        categoryNameTextView.text = categoryName

        footerActivity()

        // Fetch products from Firebase based on the categoryName
        fetchDataFromDatabase()
    }

    private fun fetchDataFromDatabase() {
        productsRef.orderByChild("productCategory").equalTo(categoryName)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productContainer.removeAllViews() // Clear previous items

                    val productList = mutableListOf<DataSnapshot>()
                    for (productSnapshot in snapshot.children) {
                        productList.add(productSnapshot)
                    }

                    // Create rows of two products each (pairs of products)
                    for (i in productList.indices step 2) {
                        val product1 = productList[i]
                        val product2 = if (i + 1 < productList.size) productList[i + 1] else null

                        // Create and add a product row view to the product container
                        val productRowView = createProductRowView(product1, product2)
                        productContainer.addView(productRowView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    // Function to get the lowest productPrice from productSpecs as a Double
    private fun getLowestProductPrice(productSpecs: DataSnapshot): Double? {
        var lowestPrice: Double? = null
        for (specSnapshot in productSpecs.children) {
            val price = specSnapshot.child("productPrice").getValue(Double::class.java)
            if (price != null) {
                if (lowestPrice == null || price < lowestPrice) {
                    lowestPrice = price
                }
            }
        }
        return lowestPrice
    }

    private fun createProductRowView(product1: DataSnapshot, product2: DataSnapshot?): LinearLayout {
        val inflater = LayoutInflater.from(this)
        val productRow = inflater.inflate(R.layout.category_details_product_box, productContainer, false) as LinearLayout

        // First product
        val product1Image: ImageView = productRow.findViewById(R.id.productImage)
        val product1Name: TextView = productRow.findViewById(R.id.productName)
        val product1Price: TextView = productRow.findViewById(R.id.productPrice)
        val productBox1: LinearLayout = productRow.findViewById(R.id.productBox1)

        // Retrieve and set the values for product 1
//        val productId1 = product1.key // Assuming the product's unique ID is the key in Firebase
        val productId1 = product1.child("id").getValue(String::class.java) // Assuming the product's unique ID is the key in Firebase
        val imageBase64 = product1.child("productImages").child("0").getValue(String::class.java)
        if (imageBase64 != null) {
            val decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT)
            val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            product1Image.setImageBitmap(decodedBitmap)
        }
        product1Name.text = product1.child("productName").getValue(String::class.java)

        // Get and display the lowest price for product 1
        val lowestPriceProduct1 = getLowestProductPrice(product1.child("productSpecs"))
        product1Price.text = if (lowestPriceProduct1 != null) {
            String.format("RM %.2f", lowestPriceProduct1) // Ensures 2 decimal places
        } else {
            "RM N/A"
        }

        // Set click listener for product 1
        productBox1.setOnClickListener {
            val intent = Intent(this, ProductDetailsActivity::class.java)
            intent.putExtra("productId", productId1)
            startActivity(intent)
        }

        // Second product (if available)
        val product2Image: ImageView? = productRow.findViewById(R.id.productImage2)
        val product2Name: TextView? = productRow.findViewById(R.id.productName2)
        val product2Price: TextView? = productRow.findViewById(R.id.productPrice2)
        val productBox2: LinearLayout? = productRow.findViewById(R.id.productBox2)

        if (product2 != null) {
            productBox2?.visibility = LinearLayout.VISIBLE
            val productId2 = product2.child("id").getValue(String::class.java) // Retrieve product 2's ID

            // Set product 2 image
            val imageBase64Product2 = product2.child("productImages").child("0").getValue(String::class.java)
            if (imageBase64Product2 != null) {
                val decodedBytesProduct2 = Base64.decode(imageBase64Product2, Base64.DEFAULT)
                val decodedBitmapProduct2 = BitmapFactory.decodeByteArray(decodedBytesProduct2, 0, decodedBytesProduct2.size)
                product2Image?.setImageBitmap(decodedBitmapProduct2)
            }
            product2Name?.text = product2.child("productName").getValue(String::class.java)

            // Get and display the lowest price for product 2
            val lowestPriceProduct2 = getLowestProductPrice(product2.child("productSpecs"))
            product2Price?.text = if (lowestPriceProduct2 != null) {
                String.format("RM %.2f", lowestPriceProduct2)
            } else {
                "RM N/A"
            }

            // Set click listener for product 2
            productBox2?.setOnClickListener {
                val intent = Intent(this, ProductDetailsActivity::class.java)
                intent.putExtra("productId", productId2)
                startActivity(intent)
            }
        } else {
            // Hide the second product box if not available
            productBox2?.visibility = LinearLayout.VISIBLE
            product2Image?.visibility = ImageView.GONE
            product2Name?.visibility = TextView.GONE
            product2Price?.visibility = TextView.GONE
            productBox2?.setBackgroundColor(resources.getColor(android.R.color.transparent))
        }

        return productRow
    }

    private fun footerActivity() {
        val homeIcon: ImageView = findViewById(R.id.homeIcon)
        val homeTextView: TextView = findViewById(R.id.homeTextView)
        val shopIcon: ImageView = findViewById(R.id.shopIcon)
        val shopTextView: TextView = findViewById(R.id.shopTextView)
        val settingIcon: ImageView = findViewById(R.id.settingIcon)
        val settingTextView: TextView = findViewById(R.id.settingTextView)
        val cartIconImageView: ImageView = findViewById(R.id.cartIcon)

        val backTextView: TextView = findViewById(R.id.backTextView)
        val navigateHeaderTextView: TextView = findViewById(R.id.logoTitleText)

        backTextView.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        navigateHeaderTextView.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
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
    }

}
