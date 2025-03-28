package com.example.arhomedecorationapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.arhomedecorationapplication.databinding.ActivityHeaderBinding

class HeaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHeaderBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_header)

        binding.logoTitleText.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val navigateTextView: TextView = findViewById(R.id.logoTitleText)
        navigateTextView.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

    }
}