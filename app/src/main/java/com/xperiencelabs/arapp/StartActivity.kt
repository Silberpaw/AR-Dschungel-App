package com.xperiencelabs.arapp

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


// Hier werden die Funktionen der Buttons gesetzt (zur Sammlung und zur AR). Außerdek wird oben
// die Mission angezeigt

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val startButton = findViewById<LinearLayout>(R.id.startButton) // Button zur AR
        startButton.setOnClickListener {

        }
        val prefs = getSharedPreferences("animal_collection", MODE_PRIVATE) // Zugriff auf die Datei
        val mission = prefs.getString("daily_mission", "Papagei") // Mission aus Datei lesen
        val missionDone = prefs.getBoolean("mission_done", false) // Mission erledigt?

        val missionText = findViewById<TextView>(R.id.textMission) // Mission anzeigen
        missionText.text = if (missionDone) {
            "Mission erfüllt: $mission" // Wenn die Mission erledigt ist, wird die Mission als erfüllt angezeigt
        } else {
            "Finde den $mission!" // Wenn die Mission noch nicht erledigt ist, wird eine angezeigt
        }

        startButton.setOnClickListener {
            // Startet die AR-Hauptaktivität
            val intent = Intent(this, MainActivity::class.java) // MainActivity ist die Hauptaktivität
            startActivity(intent) // Startet die MainActivity
        }
        val buttonSammlung = findViewById<LinearLayout>(R.id.button2) // Button zur Sammlung
        buttonSammlung.setOnClickListener {
            // Startet SammlungsAktivität
            val intent = Intent(this, CollectionActivity::class.java) // CollectionActivity ist die Sammlungs-Aktivität
            startActivity(intent)
        }
    }
}