package com.xperiencelabs.arapp

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isGone
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import java.util.*
// Sceneview Imports

import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.material.setExternalTexture
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.VideoNode
import io.github.sceneview.node.ViewNode

class MainActivity : AppCompatActivity() {

    // sceneView ist die AR-Anzeige-Oberfläche, auf der Inhalte platziert werden
    private lateinit var sceneView: ArSceneView
    // placeButton ist die Schaltfläche, um ein Modell zu platzieren.
    lateinit var placeButton: ExtendedFloatingActionButton
    // Das 3D-Modell (z.B. Vogel)
    private lateinit var modelNode: ArModelNode
    // Das Video-Modell (z.B. Ad)
    private lateinit var videoNode: VideoNode
    // Steuert die Videowiedergabe im videoNode
    private lateinit var mediaPlayer:MediaPlayer
    // Papagei redet
    private lateinit var speechBubble: TextView
    // sprechender Papagei
    private lateinit var tts: TextToSpeech


    // wird beim Start der App aufgerufen
    override fun onCreate(savedInstanceState: Bundle?) {
        // Lädt das Layout mit der AR-Ansicht und der Schaltfläche.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisiert das AR-Sichtfeld
        // und deaktiviert die Lichtschätzung (Lichtverhältnisse aus Kamera ignorieren)
        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }
        // Initialisiert das Textfeld für die Papagei
        speechBubble = findViewById(R.id.speechBubble)

        // vorlesen
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.GERMAN
            }
        }
        // Initialisiert die Videowiedergabe
        //mediaPlayer = MediaPlayer.create(this,R.raw.ad)

        // Initialisiert den Button für das Platzieren des Modells
        placeButton = findViewById(R.id.place)

        // Setzt einen Klick-Listener auf Button
        placeButton.setOnClickListener {
            // Platziert das Modell
            placeModel()
        }


        val arrowNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/arrow.glb",
                scaleToUnits = 0.5f
            )
            position = Position(x = 0.5f, y = 0f, z = -1.0f) // Richtung bestimmen
            rotation = Rotation(x = 0f, y = 90f, z = 0f) // z.B. nach rechts drehend
        }
        sceneView.addChild(arrowNode)

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // optional: Sprech-Animation starten
            }

            override fun onDone(utteranceId: String?) {
                if (utteranceId == "polly_done") {
                    // Modell austauschen – das muss auf dem UI-Thread passieren!
                    runOnUiThread {
                        replaceModelWithFlyingPolly()
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                // Fehlerbehandlung (optional)
            }
        })
        /*
        // Initialisiert das Video-Modell
        videoNode = VideoNode(sceneView.engine, scaleToUnits = 0.7f, centerOrigin = Position(y=-4f), glbFileLocation = "models/plane.glb", player = mediaPlayer, onLoaded = {_,_ ->
            // Startet die Videowiedergabe
            mediaPlayer.start()
        })
        */
        // Initialisiert das 3D-Modell
        modelNode = ArModelNode(sceneView.engine,PlacementMode.INSTANT).apply {
            // Platziert das Modell in der Scene
            loadModelGlbAsync(
                glbFileLocation = "models/toon_parrot.glb",
                scaleToUnits = 0.7f, //3 für parrot und monkey, 0.7 für toon monkey
                //centerOrigin = Position(f)


            )
            { modelNode.rotation = Rotation(0f, 280f, 0f) // richtung in die papagei guckt
                // Setzt die Farbe des Modells
                //sceneView.planeRenderer.isVisible = true
                //val materialInstance = it.materialInstances[0]
            }

            // Setzt einen Klick-Listener auf das Modell
            onAnchorChanged = {
                placeButton.isGone = it != null
            }

        }
        // Fügt das Modell zur Scene hinzu
        sceneView.addChild(modelNode)
        // Fügt das Video-Modell zur Scene hinzu
        //modelNode.addChild(videoNode)
        // Lade das Layou

    }

    // Platziert das Modell
    private fun placeModel() {
        modelNode.anchor()

        // 1. Zeigt den ersten Text und spricht ihn
        val firstText = "Hallo! Ich bin Polly. Ich zeige dir heute etwas Spannendes!"
        speechBubble.text = firstText
        speechBubble.visibility = View.VISIBLE
        tts.speak(firstText, TextToSpeech.QUEUE_FLUSH, null, null)

        // 2. Nach 4 Sekunden: neuer Text + erneut sprechen
        Handler(Looper.getMainLooper()).postDelayed({
            val secondText = "Folg mir, um den Affen zu entdecken!"
            speechBubble.text = secondText
            tts.speak(
                secondText,
                TextToSpeech.QUEUE_FLUSH,
                Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "polly_done") },
                "polly_done"
            )
        }, 8000)
        val followText = "Schau da vorne! Der Affe ist in dieser Richtung!"
        speechBubble.text = followText
        tts.speak(followText, TextToSpeech.QUEUE_FLUSH, null, null)

       // sceneView.planeRenderer.isVisible = false
    }

    private fun replaceModelWithFlyingPolly() {
        // Entferne altes Modell
        sceneView.removeChild(modelNode)

        // Neues Modell
        val monkeyNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/parrot.glb",
                scaleToUnits = 3f
            ) {
                sceneView.planeRenderer.isVisible = true
            }
        }

        sceneView.addChild(monkeyNode)
    }

    override fun onPause() {
        super.onPause()
        //mediaPlayer.stop()
    }
    override fun onDestroy() {
        super.onDestroy()
        //mediaPlayer.release()
        tts.shutdown()
    }

}