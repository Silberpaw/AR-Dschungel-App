package com.xperiencelabs.arapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val startButton = findViewById<FloatingActionButton>(R.id.startButton)

        startButton.setOnClickListener {
            // Starte die AR-Hauptaktivität
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}