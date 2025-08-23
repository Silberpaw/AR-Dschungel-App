package com.xperiencelabs.arapp

import android.os.Bundle
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CollectionActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private val allAnimals = listOf("affe", "elefant", "pinguin", "löwe")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)

        listView = findViewById(R.id.listView) // damit die tiere im layout als liste gezeigt werden

        val prefs = getSharedPreferences("animal_collection", MODE_PRIVATE)

        // Sterne
        val stars = prefs.getInt("stars", 0)
        findViewById<TextView>(R.id.textStars).text = "⭐ $stars"

        // Fortschritt
        val collectedCount = allAnimals.count { prefs.getBoolean(it, false) }
        findViewById<ProgressBar>(R.id.progressBar).apply {
            max = allAnimals.size
            progress = collectedCount
        }

        // Badge
        val badge = if (prefs.getBoolean("badge_tierprofi", false))
            "🏅 Tierprofi"
        else
            "🔓 Noch kein Titel"
        findViewById<TextView>(R.id.textBadge).text = "Titel: $badge"

        // Tiere (mit Bild, Texten, Selfie-Button)
        val animals = listOf(
            Animal("Anton", "Afrika", "25 Jahre", "Bananen", "Liebt Klettern!",
                R.drawable.monkey, "models/monkey.glb"),
            Animal("Polly", "Südamerika", "40 Jahre", "Nüsse & Früchte", "Kann sprechen!",
                R.drawable.parrot_transparent, "models/parrot.glb")
            // mehr tiere mit datenklasse erstellen, aber braucht models also lieber nicht
        )

        val adapter = AnimalAdapter(this, animals) // wenn man ein Listview macht dann brauch man einen Adapter der die Liste oder Recyclerview händelt
        listView.adapter = adapter
    }
}
