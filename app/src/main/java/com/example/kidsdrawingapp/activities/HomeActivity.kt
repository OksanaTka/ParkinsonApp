package com.example.kidsdrawingapp.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.kidsdrawingapp.activities.MainActivity.Companion.OPEN_COLOR
import com.example.kidsdrawingapp.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private var color = Color.BLUE

    private var binding: ActivityHomeBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding!!.homeBTNStart.setOnClickListener {
            openMainActivity()
        }

    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(OPEN_COLOR, color.toString())
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}