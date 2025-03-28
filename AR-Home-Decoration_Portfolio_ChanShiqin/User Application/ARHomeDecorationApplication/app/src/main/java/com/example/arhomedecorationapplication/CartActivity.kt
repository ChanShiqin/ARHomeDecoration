package com.example.arhomedecorationapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import android.util.Base64
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatButton
import java.io.Serializable

class CartActivity : AppCompatActivity() {

    private lateinit var productRef: DatabaseReference
    private lateinit var cartReference: DatabaseReference
    private var totalPrice = 0.0
    private val selectedCartItems = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        footerActivity()

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("user_id", null)

        if (userId == null) {
            Toast.makeText(this, "User not found. Please log in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        productRef = FirebaseDatabase.getInstance().getReference("product")
        cartReference = FirebaseDatabase.getInstance().getReference("user/$userId/cart")

        loadCartItems()

        val cartPaymentButton: AppCompatButton = findViewById(R.id.cartPaymentButton)
        cartPaymentButton.setOnClickListener {
            if (selectedCartItems.isEmpty()) {
                Toast.makeText(this, "No items selected for payment!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, PaymentActivity::class.java)
            val selectedItemsBundle = Bundle()
            selectedItemsBundle.putSerializable("selectedItems", ArrayList(selectedCartItems))
            intent.putExtras(selectedItemsBundle)
            startActivity(intent)
        }
    }

    private fun loadCartItems() {
        val cartItemsContainer = findViewById<LinearLayout>(R.id.cartItemsContainer)
        val scrollView: ScrollView = findViewById(R.id.scrollView)
        val noItemMessage: TextView = findViewById(R.id.no_item_message)

        cartItemsContainer.removeAllViews()

        cartReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    scrollView.visibility = View.VISIBLE
                    noItemMessage.visibility = View.GONE
                    totalPrice = 0.0
                    updateTotalPrice()

                    for (cartItemSnapshot in snapshot.children) {
                        val productId = cartItemSnapshot.child("productId").getValue(String::class.java)
                        val design = cartItemSnapshot.child("productDesign").getValue(String::class.java) ?: ""
                        val quantity = cartItemSnapshot.child("productQuantity").getValue(Int::class.java) ?: 1

                        if (productId != null) {
                            loadProductDetails(productId, design, quantity)
                        } else {
                            Toast.makeText(this@CartActivity, "Error loading cart item", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    scrollView.visibility = View.GONE
                    noItemMessage.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartActivity", "Failed to load cart items: ${error.message}")
            }
        })
    }


    private fun loadProductDetails(productId: String, design: String, quantity: Int) {
        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (productSnapshot in snapshot.children) {
                    val dbProductId = productSnapshot.child("id").getValue(String::class.java)

                    if (dbProductId == productId) {
                        val productName = productSnapshot.child("productName").getValue(String::class.java) ?: "Unknown Product"
                        val productSpecs = productSnapshot.child("productSpecs")
                        var productPrice = 0.0
                        for (specSnapshot in productSpecs.children) {
                            val productDesign = specSnapshot.child("productDesign").getValue(String::class.java)
                            if (productDesign == design) {
                                productPrice = specSnapshot.child("productPrice").getValue(Double::class.java) ?: 0.0
                                break
                            }
                        }

                        val productImages = productSnapshot.child("productImages").child("0").getValue(String::class.java)
                        val firstImageBitmap = productImages?.let { decodeBase64ToBitmap(it) }

                        displayCartItem(productId, productName, design, productPrice, quantity, firstImageBitmap)
                        break
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

    private fun displayCartItem(productId: String, productName: String, design: String, price: Double, quantity: Int, firstImageBitmap: Bitmap?) {
        val cartItemsContainer = findViewById<LinearLayout>(R.id.cartItemsContainer)
        val cartItemView = layoutInflater.inflate(R.layout.cart_item, cartItemsContainer, false)

        val productTitleTextView = cartItemView.findViewById<TextView>(R.id.productTitle)
        val productDesignTextView = cartItemView.findViewById<TextView>(R.id.productDesign)
        val productPriceTextView = cartItemView.findViewById<TextView>(R.id.productPrice)
//        val productQuantityTextView = cartItemView.findViewById<TextView>(R.id.productQuantity)
        val productQuantityEditText = cartItemView.findViewById<EditText>(R.id.productQuantity)
        val productImageView = cartItemView.findViewById<ImageView>(R.id.productImage)
        val selectItemCheckBox = cartItemView.findViewById<CheckBox>(R.id.checkbox)
        val deleteButton = cartItemView.findViewById<ImageView>(R.id.deleteButton)

        productTitleTextView.text = productName
        productDesignTextView.text = "- $design"
        productPriceTextView.text = String.format("RM %.2f", price)
//        productQuantityTextView.text = "$quantity"
        productQuantityEditText.setText(quantity.toString())

        if (firstImageBitmap != null) {
            productImageView.setImageBitmap(firstImageBitmap)
        } else {
            productImageView.setImageResource(R.drawable.product1_image)
        }

        // Handle product box click to navigate to details page
        cartItemView.setOnClickListener {
            val intent = Intent(this, ProductDetailsActivity::class.java)
            intent.putExtra("productId", productId)
            startActivity(intent)
        }

        val increaseQuantityButton = cartItemView.findViewById<TextView>(R.id.increaseQuantity)
        val decreaseQuantityButton = cartItemView.findViewById<TextView>(R.id.decreaseQuantity)

        increaseQuantityButton.setOnClickListener {
            val currentQuantity = productQuantityEditText.text.toString().toIntOrNull() ?: 1
            productRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Find the product with the matching design
                    for (productSnapshot in snapshot.children) {
                        val productSpecs = productSnapshot.child("productSpecs")
                        for (specSnapshot in productSpecs.children) {
                            val productDesign = specSnapshot.child("productDesign").getValue(String::class.java)
                            if (productDesign == design) {
                                val availableStock = specSnapshot.child("productStock").getValue(Int::class.java) ?: 0

                                // Check if the new quantity exceeds the available stock
                                val newQuantity = currentQuantity + 1
                                if (newQuantity <= availableStock) {
                                    productQuantityEditText.setText(newQuantity.toString())
                                    updateCartItemQuantity(productId, design, newQuantity)
                                } else {
                                    // Show a message or alert if the quantity exceeds the available stock
                                    Toast.makeText(this@CartActivity, "Cannot exceed available stock of $availableStock", Toast.LENGTH_SHORT).show()
                                }
                                break
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartActivity", "Failed to fetch product stock: ${error.message}")
                }
            })
        }

        // Decrease quantity
        decreaseQuantityButton.setOnClickListener {
            val currentQuantity = productQuantityEditText.text.toString().toIntOrNull() ?: 1
            if (currentQuantity > 1) {
                val newQuantity = currentQuantity - 1
                productQuantityEditText.setText(newQuantity.toString())
                updateCartItemQuantity(productId, design, newQuantity)
            }
        }

        // Store previous quantity for revert in case of invalid input
        var previousQuantity = quantity

        productQuantityEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newQuantity = s.toString().toIntOrNull() ?: 1
                productRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (productSnapshot in snapshot.children) {
                            val productSpecs = productSnapshot.child("productSpecs")
                            for (specSnapshot in productSpecs.children) {
                                val productDesign = specSnapshot.child("productDesign").getValue(String::class.java)
                                if (productDesign == design) {
                                    val availableStock = specSnapshot.child("productStock").getValue(Int::class.java) ?: 0

                                    // Check if the new quantity exceeds the available stock
                                    if (newQuantity > availableStock) {
                                        // Show a toast message and revert the quantity to previous valid value
                                        Toast.makeText(this@CartActivity, "Cannot exceed available stock of $availableStock", Toast.LENGTH_SHORT).show()
                                        productQuantityEditText.setText(previousQuantity.toString())
                                    } else {
                                        previousQuantity = newQuantity // Update previousQuantity if valid
                                        updateCartItemQuantity(productId, design, newQuantity)
                                    }
                                    break
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("CartActivity", "Failed to fetch product stock: ${error.message}")
                    }
                })
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        deleteButton.setOnClickListener {
            deleteCartItem(productId, design)
        }

        selectItemCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val currentQuantity = productQuantityEditText.text.toString().toIntOrNull() ?: 1
            val itemTotalPrice = price * currentQuantity

            if (isChecked) {
                selectedCartItems.add(CartItem(productId, productName, design, price, currentQuantity))
                totalPrice += itemTotalPrice
            } else {
                selectedCartItems.removeIf { it.productId == productId && it.productDesign == design }
                totalPrice -= itemTotalPrice
            }
            updateTotalPrice()
        }


        cartItemsContainer.addView(cartItemView)
    }

    private fun updateCartItemQuantity(productId: String, design: String, newQuantity: Int) {
        cartReference.orderByChild("productId").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cartItemSnapshot in snapshot.children) {
                        val itemDesign = cartItemSnapshot.child("productDesign").getValue(String::class.java)
                        if (itemDesign == design) {
                            cartItemSnapshot.ref.child("productQuantity").setValue(newQuantity)
                                .addOnSuccessListener {
//                                    recalculateTotalPrice()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@CartActivity, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                                }
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartActivity", "Failed to update quantity: ${error.message}")
                }
            })
    }

    private fun deleteCartItem(productId: String, design: String) {
        cartReference.orderByChild("productId").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (cartItemSnapshot in snapshot.children) {
                        val itemDesign = cartItemSnapshot.child("productDesign").getValue(String::class.java)
                        if (itemDesign == design) {
                            cartItemSnapshot.ref.removeValue().addOnSuccessListener {
                                Toast.makeText(this@CartActivity, "Item removed from cart", Toast.LENGTH_SHORT).show()
                                loadCartItems()
                            }.addOnFailureListener {
                                Toast.makeText(this@CartActivity, "Failed to remove item", Toast.LENGTH_SHORT).show()
                            }
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartActivity", "Failed to delete item: ${error.message}")
                }
            })
    }

    private fun updateTotalPrice() {
        val totalPriceTextView = findViewById<TextView>(R.id.totalPriceTextView)
        totalPriceTextView.text = String.format("RM %.2f", totalPrice)
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
        cartIconImageView.setOnClickListener { recreate() }
    }
}

data class CartItem(
    val productId: String,
    val productName: String,
    val productDesign: String,
    val productPrice: Double,
    var productQuantity: Int,
    val imageUrl: String? = null
) : Serializable
