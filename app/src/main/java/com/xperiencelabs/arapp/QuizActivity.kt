package com.xperiencelabs.arapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        findViewById<TextView>(R.id.quizQuestion).text = "Wie viele Beine hat ein Affe?"

        findViewById<Button>(R.id.answer1).setOnClickListener {
            Toast.makeText(this, "Falsch!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.answer2).setOnClickListener {
            Toast.makeText(this, "Richtig! Du hast den Affen gefangen!", Toast.LENGTH_LONG).show()
            // Hier könntest du später den Affen zur Sammlung speichern
            finish()
        }

        findViewById<Button>(R.id.answer3).setOnClickListener {
            Toast.makeText(this, "Leider falsch.", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.answer4).setOnClickListener {
            Toast.makeText(this, "Nein, versuch’s nochmal!", Toast.LENGTH_SHORT).show()
        }
    }
}