package com.example.arhomedecorationapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class FooterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_footer)

        val homeIcon: ImageView = findViewById(R.id.homeIcon)
        val homeTextView: TextView = findViewById(R.id.homeTextView)
        val shopIcon: ImageView = findViewById(R.id.shopIcon)
        val shopTextView: TextView = findViewById(R.id.shopTextView)
        val settingIcon: ImageView = findViewById(R.id.settingIcon)
        val settingTextView: TextView = findViewById(R.id.settingTextView)

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
    }
}