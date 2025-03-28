package com.example.arhomedecorationapplication

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gorisse.thomas.sceneform.environment.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class ArViewActivity : AppCompatActivity() {

    private lateinit var productRef: DatabaseReference
    private var productId: String? = null

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_view)

        val backTextView: TextView = findViewById(R.id.backTextView)
        backTextView.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        productRef = FirebaseDatabase.getInstance().getReference("product")
        productId = intent.getStringExtra("productId")

        if (productId == null) {
            findViewById<TextView>(R.id.productTitleTextView).text = "Product ID is missing"
            return
        }

        // Step 3: Query the database for productAR based on productId
        productRef.orderByChild("id").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val productAR = childSnapshot.child("productAR").value as? String
                            if (!productAR.isNullOrEmpty()) {
                                // Successfully retrieved productAR
                                Log.d("ArViewActivity", "productAR: $productAR")
//                                findViewById<TextView>(R.id.productTitleTextView).text = "Product AR: $productAR"

                                // Load model.glb from assets folder or http url
                                (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)
                                    .setOnTapPlaneGlbModel(productAR)

                                return
                            }
                        }
                        findViewById<TextView>(R.id.productTitleTextView).text = "AR model not available"
                    } else {
                        findViewById<TextView>(R.id.productTitleTextView).text = "Product not found"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ArViewActivity", "Database error: ${error.message}")
                    findViewById<TextView>(R.id.productTitleTextView).text = "Error fetching data"
                }
            })

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        val captureButton: Button = findViewById(R.id.captureButton)

        captureButton.setOnClickListener {
            takePhoto()
        }

//
//        // Load model.glb from assets folder or http url
//        (supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment)
//            .setOnTapPlaneGlbModel("models/arm_chair.glb")
    }

    private fun takePhoto() {
        val sceneView = arFragment.arSceneView
        val bitmap = Bitmap.createBitmap(sceneView.width, sceneView.height, Bitmap.Config.ARGB_8888)

        PixelCopy.request(sceneView, bitmap, { result ->
            if (result == PixelCopy.SUCCESS) {
                saveBitmap(bitmap)
                Toast.makeText(this, "Photo saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to capture screenshot", Toast.LENGTH_SHORT).show()
            }
        }, Handler(mainLooper))
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "AR_Screenshot_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AR_Screenshots")
        }

        val contentResolver: ContentResolver = applicationContext.contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            try {
                val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                outputStream?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
