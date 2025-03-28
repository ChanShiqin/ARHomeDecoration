package com.example.arhomedecorationapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var userRef: DatabaseReference
    private lateinit var orderRef: DatabaseReference
    private lateinit var productRef: DatabaseReference
    private lateinit var orderHistoryLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)
        footerActivity()

        // Initialize references
        userRef = FirebaseDatabase.getInstance().getReference("user")

        orderHistoryLayout = findViewById(R.id.OrderItemContainer)
        orderRef = FirebaseDatabase.getInstance().getReference("order")
        productRef = FirebaseDatabase.getInstance().getReference("product")

        fetchUserOrders()
    }

    private fun fetchUserOrders() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (!userId.isNullOrEmpty()) {
            orderRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val orders = snapshot.children.toList()
                        val sortedOrders = orders.sortedByDescending {
                            it.child("orderTime").getValue(String::class.java) ?: "0"
                        }

                        if (sortedOrders.isEmpty()) {
                            showToast("No recent orders found.")
                        } else {
                            sortedOrders.forEach { displayOrder(it) }
                        }
                    } else {
                        showToast("No orders found.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Failed to load orders: ${error.message}")
                }
            })
        } else {
            showToast("User ID is missing. Please log in again.")
        }
    }

    private fun displayOrder(orderSnapshot: DataSnapshot) {
        val orderId = orderSnapshot.child("id").getValue(String::class.java) ?: "Unknown ID"
        val totalPrice = orderSnapshot.child("totalPrice").getValue(Double::class.java) ?: 0
        val orderTime = orderSnapshot.child("orderTime").getValue(String::class.java) ?: "Unknown Time"

        // Calculate the total quantity of all items in the order
        val itemsSnapshotForTotal = orderSnapshot.child("items").children
        var totalQuantity = 0
        itemsSnapshotForTotal.forEach {
            totalQuantity += it.child("productQuantity").getValue(Int::class.java) ?: 0
        }

        // Get the latest order status
        val statusSnapshot = orderSnapshot.child("orderStatus")
        val latestStatus = statusSnapshot.children.lastOrNull()
        val statusText = latestStatus?.child("updateStatus")?.getValue(String::class.java) ?: "Unknown Status"

        // Get the first product in the order items
        val itemsSnapshot = orderSnapshot.child("items").children.firstOrNull()
        val productId = itemsSnapshot?.child("productId")?.getValue(String::class.java) ?: "Unknown Product ID"
        val productName = itemsSnapshot?.child("productName")?.getValue(String::class.java) ?: "Unknown Product"
        val productPrice = itemsSnapshot?.child("productPrice")?.getValue(Double::class.java) ?: 0
        val productQuantity = itemsSnapshot?.child("productQuantity")?.getValue(Int::class.java) ?: 0
        val productDesign = itemsSnapshot?.child("productDesign")?.getValue(String::class.java) ?: "N/A"

        val formattedProductPrice = String.format("%.2f", productPrice.toDouble())
        val formattedTotalPrice = String.format("%.2f", totalPrice.toDouble())

        val orderView = layoutInflater.inflate(R.layout.order_item_view, orderHistoryLayout, false)

        orderView.findViewById<TextView>(R.id.productOrderStatus).text = "$statusText"
        orderView.findViewById<TextView>(R.id.productName).text = "$productName"
        orderView.findViewById<TextView>(R.id.productPrice).text = "RM $formattedProductPrice"
        orderView.findViewById<TextView>(R.id.productQuantity).text = "x $productQuantity"
        orderView.findViewById<TextView>(R.id.productDesign).text = "$productDesign"
        orderView.findViewById<TextView>(R.id.totalProductPrice).text = "Total $totalQuantity items: RM $formattedTotalPrice"

        orderHistoryLayout.addView(orderView)

        // Find the viewDetails button within the orderView
        val viewDetails: TextView = orderView.findViewById(R.id.viewDetails)

        // Set a click listener to navigate to the OrderHistoryProductDetailsActivity with the orderId
        viewDetails.setOnClickListener {
            val intent = Intent(this, OrderHistoryProductDetailsActivity::class.java)
            intent.putExtra("orderId", orderId) // Pass the orderId as an extra
            startActivity(intent)
        }

        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot.children) {
                    val dbProductId = productSnapshot.child("id").getValue(String::class.java)

                    if (dbProductId == productId) {


                        val imageBase64 = productSnapshot.child("productImages").child("0").getValue(String::class.java)
                        val productImageView = orderView.findViewById<ImageView>(R.id.productImage)

                        if (!imageBase64.isNullOrEmpty()) {
                            val decodedBitmap = decodeBase64ToBitmap(imageBase64)
                            if (decodedBitmap != null) {
                                productImageView.setImageBitmap(decodedBitmap)
                            } else {
                                productImageView.setImageResource(R.drawable.product1_image) // Default image placeholder
                            }
                        } else {
                            productImageView.setImageResource(R.drawable.product1_image) // Default image placeholder
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartActivity", "Failed to load product details: ${error.message}")
            }
        })
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
}