package com.example.arhomedecorationapplication

import OrderStatusAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class OrderHistoryProductDetailsActivity : AppCompatActivity() {

//    private var ordertId: String? = null
    private lateinit var orderId: String

    private lateinit var userRef: DatabaseReference
    private lateinit var orderStatusRef: DatabaseReference
    private lateinit var orderRef: DatabaseReference
    private lateinit var productRef: DatabaseReference

    private lateinit var shippingStatusRecyclerView: RecyclerView
    private lateinit var orderStatusAdapter: OrderStatusAdapter
    private val orderStatusList = mutableListOf<OrderStatus>()

    private lateinit var productDetailsItemContainer: LinearLayout

    private lateinit var totalPriceText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history_product_details)
        footerActivity()

//        ordertId = intent.getStringExtra("orderId")
        orderId = intent.getStringExtra("orderId") ?: return

        // Initialize references
        userRef = FirebaseDatabase.getInstance().getReference("user")
        orderStatusRef = FirebaseDatabase.getInstance().getReference("order/$orderId/orderStatus")
//        orderProductRef = FirebaseDatabase.getInstance().getReference("order/$ordertId/items")
        productRef = FirebaseDatabase.getInstance().getReference("product")

        // Initialize RecyclerView
        shippingStatusRecyclerView = findViewById(R.id.shippingStatusRecyclerView)
        shippingStatusRecyclerView.layoutManager = LinearLayoutManager(this)

        // Adapter setup
        orderStatusAdapter = OrderStatusAdapter(orderStatusList)
        shippingStatusRecyclerView.adapter = orderStatusAdapter

        // Fetch data from Firebase
        fetchOrderStatus()

        productDetailsItemContainer = findViewById(R.id.productDetailsItemContainer)

        orderRef = FirebaseDatabase.getInstance().getReference("order/$orderId/items")
        productRef = FirebaseDatabase.getInstance().getReference("product")
        productDetailsItemContainer = findViewById(R.id.productDetailsItemContainer)

        // Fetch order items and display them
        fetchOrderItems()

        totalPriceText = findViewById(R.id.totalPriceText)
        fetchTotalPrice()

    }

    private fun fetchOrderStatus() {
        orderStatusRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderStatusList.clear()
                for (statusSnapshot in snapshot.children) {
                    val updateStatus = statusSnapshot.child("updateStatus").value.toString()
                    val updateTime = statusSnapshot.child("updateTime").value.toString()
                    orderStatusList.add(OrderStatus(updateStatus, updateTime))
                }
                orderStatusAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }

//    private fun fetchOrderItems() {
//        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (itemSnapshot in snapshot.children) {
//                    val productId = itemSnapshot.child("productId").value.toString()
//                    val productDesign = itemSnapshot.child("productDesign").value.toString()
//                    val productName = itemSnapshot.child("productName").value.toString()
//                    val productPrice = itemSnapshot.child("productPrice").value.toString().toDouble()
//                    val productQuantity = itemSnapshot.child("productQuantity").value.toString().toInt()
//
//                    // Fetch product images using the productId
////                    fetchProductImage(productId, productName, productDesign, productPrice, productQuantity)
//
//                    val itemView = LayoutInflater.from(this@OrderHistoryProductDetailsActivity)
//                        .inflate(R.layout.order_history_product_item_view, productDetailsItemContainer, false)
//
////                    val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
//                    val productNameText: TextView = itemView.findViewById(R.id.productNameText)
//                    val productDesignText: TextView = itemView.findViewById(R.id.productDesignText)
//                    val productQuantityText: TextView = itemView.findViewById(R.id.productQuantityText)
//                    val productPriceText: TextView = itemView.findViewById(R.id.productPriceText)
//
//                    val formattedProductPrice = String.format("%.2f", productPrice)
//
//                    // Set data in the item view
//                    productNameText.text = productName
//                    productDesignText.text = productDesign
//                    productQuantityText.text = "x$productQuantity"
//                    productPriceText.text = "RM $formattedProductPrice"
//
//                    // Set the click listener on the product details container (or another clickable element)
//                    val productDetailsItemContainer: LinearLayout = itemView.findViewById(R.id.orderHistoryProductDetailsBox)
//
//                    productDetailsItemContainer.setOnClickListener {
//                        val intent = Intent(this@OrderHistoryProductDetailsActivity, ProductDetailsActivity::class.java)
//                        intent.putExtra("productId", productId) // Pass the productId as an extra
//                        startActivity(intent)
//                    }
//
//                    productRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            for (productSnapshot in snapshot.children) {
//                                val dbProductId = productSnapshot.child("id").getValue(String::class.java)
//
//                                if (dbProductId == productId) {
//
//
//                                    val imageBase64 = productSnapshot.child("productImages").child("0").getValue(String::class.java)
//                                    val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)
//
//                                    if (!imageBase64.isNullOrEmpty()) {
//                                        val decodedBitmap = decodeBase64ToBitmap(imageBase64)
//                                        if (decodedBitmap != null) {
//                                            productImageView.setImageBitmap(decodedBitmap)
//                                        } else {
//                                            productImageView.setImageResource(R.drawable.product1_image) // Default image placeholder
//                                        }
//                                    } else {
//                                        productImageView.setImageResource(R.drawable.product1_image) // Default image placeholder
//                                    }
//                                }
//                            }
//                        }
//                        override fun onCancelled(error: DatabaseError) {
//                            Log.e("CartActivity", "Failed to load product details: ${error.message}")
//                        }
//                    })
//
//                    // Add the item view to the container
//                    productDetailsItemContainer.addView(itemView)
//
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@OrderHistoryProductDetailsActivity, "Failed to load order items", Toast.LENGTH_SHORT).show()
//            }
//
//
//        })
//    }

//    private fun fetchOrderItems() {
//        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (itemSnapshot in snapshot.children) {
//                    val productId = itemSnapshot.child("productId").value.toString()
//                    val productDesign = itemSnapshot.child("productDesign").value.toString()
//                    val productName = itemSnapshot.child("productName").value.toString()
//                    val productPrice = itemSnapshot.child("productPrice").value.toString().toDouble()
//                    val productQuantity = itemSnapshot.child("productQuantity").value.toString().toInt()
//
//                    val itemView = LayoutInflater.from(this@OrderHistoryProductDetailsActivity)
//                        .inflate(R.layout.order_history_product_item_view, productDetailsItemContainer, false)
//
//                    val productNameText: TextView = itemView.findViewById(R.id.productNameText)
//                    val productDesignText: TextView = itemView.findViewById(R.id.productDesignText)
//                    val productQuantityText: TextView = itemView.findViewById(R.id.productQuantityText)
//                    val productPriceText: TextView = itemView.findViewById(R.id.productPriceText)
//
//                    val formattedProductPrice = String.format("%.2f", productPrice)
//
//                    productNameText.text = productName
//                    productDesignText.text = productDesign
//                    productQuantityText.text = "x$productQuantity"
//                    productPriceText.text = "RM $formattedProductPrice"
//
//                    productRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            for (productSnapshot in snapshot.children) {
//                                val dbProductId = productSnapshot.child("id").getValue(String::class.java)
//
//                                if (dbProductId == productId) {
//                                    val imageBase64 = productSnapshot.child("productImages").child("0").getValue(String::class.java)
//                                    val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)
//
//                                    if (!imageBase64.isNullOrEmpty()) {
//                                        val decodedBitmap = decodeBase64ToBitmap(imageBase64)
//                                        if (decodedBitmap != null) {
//                                            productImageView.setImageBitmap(decodedBitmap)
//                                        } else {
//                                            productImageView.setImageResource(R.drawable.product1_image) // Default image placeholder
//                                        }
//                                    } else {
//                                        productImageView.setImageResource(R.drawable.product1_image) // Default image placeholder
//                                    }
//                                }
//                            }
//                        }
//                        override fun onCancelled(error: DatabaseError) {
//                            Log.e("CartActivity", "Failed to load product details: ${error.message}")
//                        }
//                    })
//
//                    // Add the item view to the container
//                    productDetailsItemContainer.addView(itemView)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(this@OrderHistoryProductDetailsActivity, "Failed to load order items", Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        // here to paste, gpt, how to do it here? thankyou
//    }

    private fun fetchOrderItems() {
        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    val productId = itemSnapshot.child("productId").value.toString()
                    val productDesign = itemSnapshot.child("productDesign").value.toString()
                    val productName = itemSnapshot.child("productName").value.toString()
                    val productPrice = itemSnapshot.child("productPrice").value.toString().toDouble()
                    val productQuantity = itemSnapshot.child("productQuantity").value.toString().toInt()

                    val itemView = LayoutInflater.from(this@OrderHistoryProductDetailsActivity)
                        .inflate(R.layout.order_history_product_item_view, productDetailsItemContainer, false)

                    val productNameText: TextView = itemView.findViewById(R.id.productNameText)
                    val productDesignText: TextView = itemView.findViewById(R.id.productDesignText)
                    val productQuantityText: TextView = itemView.findViewById(R.id.productQuantityText)
                    val productPriceText: TextView = itemView.findViewById(R.id.productPriceText)

                    val formattedProductPrice = String.format("%.2f", productPrice)

                    productNameText.text = productName
                    productDesignText.text = productDesign
                    productQuantityText.text = "x$productQuantity"
                    productPriceText.text = "RM $formattedProductPrice"

                    productRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (productSnapshot in snapshot.children) {
                                val dbProductId = productSnapshot.child("id").getValue(String::class.java)

                                if (dbProductId == productId) {
                                    val imageBase64 = productSnapshot.child("productImages").child("0").getValue(String::class.java)
                                    val productImageView = itemView.findViewById<ImageView>(R.id.productImageView)

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

                    // Add the item view to the container
                    productDetailsItemContainer.addView(itemView)

                    // Set the click listener for navigating to ProductDetailsActivity
                    itemView.setOnClickListener {
                        navigateToProductDetailsActivity(productId) // Call the function to navigate outside of the listener
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OrderHistoryProductDetailsActivity, "Failed to load order items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Method to navigate outside of the Firebase listener (on the main thread)
    private fun navigateToProductDetailsActivity(productId: String) {
        val intent = Intent(this@OrderHistoryProductDetailsActivity, ProductDetailsActivity::class.java)
        intent.putExtra("productId", productId) // Pass the productId as an extra
        startActivity(intent)
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

    private fun fetchTotalPrice() {
        val orderTotalPriceRef = FirebaseDatabase.getInstance().getReference("order/$orderId/totalPrice")

        orderTotalPriceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val totalPrice = snapshot.value.toString().toDouble()
//                    // Update the totalPriceText TextView
//                    totalPriceText.text = "Order Total: RM $totalPrice"
                    val formattedTotalPrice = String.format("%.2f", totalPrice)
                    totalPriceText.text = "Order Total: RM $formattedTotalPrice"

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OrderHistoryProductDetailsActivity, "Failed to load total price", Toast.LENGTH_SHORT).show()
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

data class OrderStatus(val updateStatus: String, val updateTime: String)
