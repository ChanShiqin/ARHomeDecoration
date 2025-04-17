package com.example.arhomedecorationapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.arhomedecorationapplication.databinding.ActivityPaymentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stripe.android.PaymentConfiguration
import kotlinx.coroutines.launch
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : AppCompatActivity(){

    private lateinit var userRef: DatabaseReference
    lateinit var paymentSheet: PaymentSheet
    lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    lateinit var paymentIntentClientSecret: String

    private var selectedItems: ArrayList<CartItem>? = null
    private var selectedCartItems: ArrayList<CartItem> = arrayListOf()
    private lateinit var cartReference: DatabaseReference
    private lateinit var productRef: DatabaseReference

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        footerActivity()

        val database = FirebaseDatabase.getInstance().reference

        // Retrieve user ID from SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        cartReference = FirebaseDatabase.getInstance().reference.child("user/$userId/cart")

        // Initialize Firebase reference
        val firebaseDatabase = FirebaseDatabase.getInstance()
        userRef = firebaseDatabase.getReference("user/$userId")

        // Find the Add Address button
//        val addAddressButton: Button = findViewById(R.id.addAddressButton)

//        // Set an onClickListener on the Add Address button
//        addAddressButton.setOnClickListener {
//            if (userId != null) {
//                showAddAddressDialog(userId)  // Pass userId to the dialog
//            } else {
//                Toast.makeText(this, "User ID is not available", Toast.LENGTH_SHORT).show()
//            }
//        }

//        showDefaultAddress()
//        shippingAddress(userId)

        // Retrieve the selected cart items from the Intent and store them
        selectedItems = intent.getSerializableExtra("selectedItems") as? ArrayList<CartItem>

        // Ensure items are retrieved and displayed correctly
        selectedItems?.let {
            Log.d("PaymentActivity", "Received Items: $it")
            displaySelectedItems(it)
            calculateTotalPrice(it)
            fetchPaymentDetails(it)
        } ?: run {
            Log.e("PaymentActivity", "No items received from intent!")
        }

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        // Fetch payment details only if selectedItems are not null
        selectedItems?.let {
            fetchPaymentDetails(it)
        }

        val payButton = findViewById<Button>(R.id.payButton)
        payButton.setOnClickListener{
            presentPaymentSheet()
        }

    }

    private fun displaySelectedItems(selectedItems: ArrayList<CartItem>) {

        val itemsContainer = findViewById<LinearLayout>(R.id.productContainer) // Parent view for the items

        selectedItems.forEach { cartItem ->
            // Inflate the product_box layout
            val itemView = layoutInflater.inflate(R.layout.product_box, itemsContainer, false)

            // Find views within the inflated layout
            val productNameTextView = itemView.findViewById<TextView>(R.id.productTitle)
            val productPriceTextView = itemView.findViewById<TextView>(R.id.productPrice)
            val productImageView = itemView.findViewById<ImageView>(R.id.productImage)

            // Set product details
            productNameTextView.text = "${cartItem.productName} [${cartItem.productDesign}] x${cartItem.productQuantity}"
            productPriceTextView.text = "RM ${String.format("%.2f", cartItem.productPrice * cartItem.productQuantity)}"

            // Load product image (if the URL is provided, use Glide; otherwise, a placeholder)
            if (!cartItem.imageUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(cartItem.imageUrl)
                    .placeholder(R.drawable.product1_image) // Replace with your placeholder image
                    .into(productImageView)
            } else {
                productImageView.setImageResource(R.drawable.product1_image)
            }

            // 🔁 Fetch product image (Base64) from Firebase using productId
            val productId = cartItem.productId
            val productRef = FirebaseDatabase.getInstance().getReference("product") // Adjust path if needed

            productRef.orderByChild("id").equalTo(productId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (productSnapshot in snapshot.children) {
                            val imageBase64 = productSnapshot.child("productImages").child("0").getValue(String::class.java)
                            if (!imageBase64.isNullOrEmpty()) {
                                val bitmap = decodeBase64ToBitmap(imageBase64)
                                if (bitmap != null) {
                                    productImageView.setImageBitmap(bitmap)
                                } else {
                                    productImageView.setImageResource(R.drawable.product1_image)
                                }
                            } else {
                                productImageView.setImageResource(R.drawable.product1_image)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("CartActivity", "Image load error: ${error.message}")
                        productImageView.setImageResource(R.drawable.product1_image)
                    }
                })

            // Add the inflated view to the container
            itemsContainer.addView(itemView)
        }

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

    private fun calculateTotalPrice(selectedItems: ArrayList<CartItem>) {
        val totalPriceTextView = findViewById<TextView>(R.id.totalPriceTextView)
        val totalPrice = selectedItems.sumOf { it.productPrice * it.productQuantity }
        totalPriceTextView.text = "RM ${String.format("%.2f", totalPrice)}"
    }

    private fun fetchPaymentDetails(selectedItems: ArrayList<CartItem>) {
        lifecycleScope.launch {
            try {
                val totalPrice = selectedItems.sumOf { it.productPrice * it.productQuantity }

                val userName = "John Doe" // Replace with actual user name
                val userEmail = "john.doe@example.com" // Replace with actual user email
                val amount = (totalPrice * 100).toInt() // Now using the totalPrice calculated earlier

                // Make the API call
                val response = RetrofitInstance.api.fetchPaymentDetails(userName, userEmail, amount)

                if (response.isSuccessful) {
                    val paymentResponse = response.body()

                    if (paymentResponse != null) {
                        val customerId = paymentResponse.customer
                        val ephemeralKey = paymentResponse.ephemeralKey
                        val paymentIntent = paymentResponse.paymentIntent
                        val publishableKey = paymentResponse.publishableKey

                        paymentIntentClientSecret = paymentIntent
                        customerConfig = PaymentSheet.CustomerConfiguration(customerId, ephemeralKey)
                        PaymentConfiguration.init(this@PaymentActivity, publishableKey)

                        Toast.makeText(this@PaymentActivity, "Payment details fetched successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PaymentActivity, "Empty response body", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PaymentActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PaymentActivity, "Something went wrong: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        // implemented in the next steps
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                print("Canceled")
                Toast.makeText(this@PaymentActivity, "Payment Cancelled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                print("Error: ${paymentSheetResult.error}")
                Toast.makeText(this@PaymentActivity, "Payment Failed", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                print("Completed")
                Toast.makeText(this@PaymentActivity, "Payment Successful", Toast.LENGTH_SHORT).show()

                // Save payment details to Firebase
                savePaymentDataToFirebase()

            }
        }
    }

    private fun savePaymentDataToFirebase() {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId != null && selectedItems != null) {
            val database = FirebaseDatabase.getInstance().reference
            val orderRef = database.child("order")
            val cartRef = database.child("user").child(userId).child("cart") // Assuming the cart is stored under user's node

            orderRef.get().addOnSuccessListener { snapshot ->
                var lastOrderId = "O00000"
                snapshot.children.forEach { userSnapshot ->
                    val orderId = userSnapshot.child("id").getValue(String::class.java)
                    if (orderId != null && orderId.startsWith("O")) {
                        if (orderId > lastOrderId) {
                            lastOrderId = orderId
                        }
                    }
                }

                val newOrderId = generateNextOrderId(lastOrderId)
                val totalPrice = selectedItems?.sumOf { it.productPrice * it.productQuantity } ?: 0.0
                val orderStatus = "Paid"
                val orderTimeMillis = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val orderTime = dateFormat.format(Date(orderTimeMillis))

                val itemsData = selectedItems?.map { cartItem ->
                    mapOf(
                        "productId" to cartItem.productId,
                        "productName" to cartItem.productName,
                        "productQuantity" to cartItem.productQuantity,
                        "productPrice" to cartItem.productPrice,
//                        "productPrice" to "RM ${String.format("%.2f", cartItem.productPrice)}",
                        "productDesign" to cartItem.productDesign,
                        "imageUrl" to cartItem.imageUrl
                    )
                } ?: emptyList()

                // Create the orderStatus array with the first status
                val orderStatusData = listOf(
                    mapOf(
                        "updateStatus" to "Paid",
                        "updateTime" to orderTime
                    )
                )

                val orderData = mapOf(
                    "id" to newOrderId,
                    "userId" to userId,
                    "totalPrice" to totalPrice,
//                    "totalPrice" to "RM ${String.format("%.2f", totalPrice)}",
                    "orderStatus" to orderStatusData,
                    "orderTime" to orderTime,
                    "items" to itemsData
                )

                orderRef.child(newOrderId).setValue(orderData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Order saved successfully!", Toast.LENGTH_SHORT).show()

                        updateProductStock()

                        // Remove items from the cart after order is saved
//                        removeItemsFromCart(cartRef, selectedItems!!)
                        // Remove selected items from the cart
                        removePaidCartItems()

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save order", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    // Helper function to generate the next order ID
    private fun generateNextOrderId(lastOrderId: String): String {
        val numberPart = lastOrderId.substring(1).toInt()
        val newNumber = numberPart + 1
        return "O" + newNumber.toString().padStart(5, '0')
    }

    private fun updateProductStock() {
        selectedItems?.let { cartItems ->
            for (item in cartItems) {
                // Get a reference to the specific product and order by productId in productSpecs
                val productRef = FirebaseDatabase.getInstance().getReference("product")
                    .orderByChild("id")
                    .equalTo(item.productId) // Use equalTo to match the productId

                // Add listener to get the productSpecs and update stock
                productRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var stockUpdated = false

                        // Loop through the productSpecs to find the correct design and update stock
                        for (productSnapshot in snapshot.children) {
                            val productSpecs = productSnapshot.child("productSpecs")
                            for (specSnapshot in productSpecs.children) {
                                val productDesign = specSnapshot.child("productDesign").getValue(String::class.java)
                                val productStock = specSnapshot.child("productStock").getValue(Int::class.java) ?: 0

                                // Match the selected productDesign with the one in the database
                                if (productDesign == item.productDesign) {
                                    val updatedStock = productStock - item.productQuantity

                                    // Only update if stock is not negative
                                    if (updatedStock >= 0) {
                                        specSnapshot.ref.child("productStock").setValue(updatedStock)
                                            .addOnSuccessListener {
                                                // Stock updated successfully
                                                stockUpdated = true
                                            }
                                            .addOnFailureListener { exception ->
                                                // Handle failure in updating stock
                                                Log.e("UpdateStock", "Failed to update stock for product: ${item.productId}, error: ${exception.message}")
                                            }
                                        break
                                    } else {
                                        // Handle case where stock is not enough
                                        Log.e("UpdateStock", "Not enough stock for product: ${item.productId}")
                                    }
                                }
                            }
                        }

//                        // Show success or failure based on whether the stock was updated
//                        if (stockUpdated) {
//                            Toast.makeText(this@PaymentActivity, "Stock updated successfully!", Toast.LENGTH_SHORT).show()
//                        } else {
//                            Toast.makeText(this@PaymentActivity, "Stock update failed, check product availability", Toast.LENGTH_SHORT).show()
//                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                        Log.e("DatabaseError", "Error fetching product stock: ${error.message}")
                        Toast.makeText(this@PaymentActivity, "Error fetching product stock", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    private fun removePaidCartItems() {
        selectedItems?.let { cartItems ->
            for (item in cartItems) {
                cartReference.orderByChild("productId").equalTo(item.productId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (cartItemSnapshot in snapshot.children) {
                                val itemDesign = cartItemSnapshot.child("productDesign").getValue(String::class.java)
                                if (itemDesign == item.productDesign) {
                                    cartItemSnapshot.ref.removeValue().addOnSuccessListener {
                                        Toast.makeText(
                                            this@PaymentActivity,
                                            "Removed: ${item.productName}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        backToCartPage()
                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            this@PaymentActivity,
                                            "Failed to remove ${item.productName}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    break
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@PaymentActivity,
                                "Failed to remove items: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        } ?: run {
            Toast.makeText(this, "No items to remove!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun backToCartPage() {
        // Navigate back to the cart page after removing items
        val intent = Intent(this, CartActivity::class.java) // Replace CartActivity with your cart page activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "AuraDecor.",
                customer = customerConfig,
            )
        )
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
