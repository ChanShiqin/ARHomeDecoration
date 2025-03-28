// StartPageActivity.kt
package com.example.arhomedecorationapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class StartPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)

        // Find the button by its ID
        val navigateButton: Button = findViewById(R.id.startButton)

        // Set an OnClickListener on the button
        navigateButton.setOnClickListener {
            // Create an intent to navigate to SecondActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent) // Start the SecondActivity
            finish()
        }
    }
}
