package com.xperiencelabs.arapp

import android.media.MediaPlayer
import android.os.*
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.CameraNode
import io.github.sceneview.node.VideoNode
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var findApeButton: ExtendedFloatingActionButton

    private lateinit var modelNode: ArModelNode
    private lateinit var videoNode: VideoNode
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var speechBubble: TextView
    private lateinit var tts: TextToSpeech
    private var apeAlreadyPlaced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView)

        // Direkt und sicher konfigurieren
        sceneView.configureSession { session, config ->
            try {
                val inputStream = assets.open("augmentedimages/marker_affe.imgdb")
                val db = AugmentedImageDatabase.deserialize(session, inputStream)
                config.augmentedImageDatabase = db
                Log.d("AR_DEBUG", "Image-Datenbank erfolgreich geladen!")
            } catch (e: Exception) {
                Log.e("AR_DEBUG", "Fehler beim Laden der Image-Datenbank: ${e.message}")
            }
        }
        speechBubble = findViewById(R.id.speechBubble)

        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) tts.language = Locale.GERMAN
        }

        placeButton = findViewById(R.id.place)
        placeButton.setOnClickListener {
            placeModel()
        }

        findApeButton = findViewById(R.id.btnFindApe)
        findApeButton.setOnClickListener {
            resetScene()
            checkOnceForApe()
        }

        val arrowNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync("models/arrow.glb", scaleToUnits = 0.5f)
            position = Position(x = 0.5f, y = 0f, z = -1.0f)
            rotation = Rotation(0f, 90f, 0f)
        }
        sceneView.addChild(arrowNode)

        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                if (utteranceId == "polly_done") {
                    runOnUiThread { replaceModelWithFlyingPolly() }
                }
            }

            override fun onError(utteranceId: String?) {}
        })

        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync("models/toon_parrot.glb", scaleToUnits = 0.7f) {
                modelNode.rotation = Rotation(0f, 280f, 0f)
            }
            onAnchorChanged = {
                placeButton.isGone = it != null
            }
        }
        sceneView.addChild(modelNode)
    }
    private fun checkOnceForApe() {
        Log.d("AR_DEBUG", "🔍 Starte Marker-Scan...")

        val handler = Handler(Looper.getMainLooper())
        var attempt = 0
        val maxAttempts = 20  // Versuche erhöhen für mehr Robustheit

        val scanRunnable = object : Runnable {
            override fun run() {
                try {
                    val frame = sceneView.arSession?.update()
                    if (frame == null) {
                        Log.d("AR_DEBUG", "⚠️ Kein Frame erhalten.")
                        handler.postDelayed(this, 300)
                        return
                    }

                    val images = frame.getUpdatedTrackables(AugmentedImage::class.java)

                    if (images.isEmpty()) {
                        Log.d("AR_DEBUG", "🔄 Versuch $attempt – Keine Trackables gefunden.")
                    } else {
                        for (image in images) {
                            Log.d("AR_DEBUG", "📸 Versuch $attempt – Bild: ${image.name} | Status: ${image.trackingState}")

                            val imageName = image.name.lowercase()
                            if (image.trackingState == TrackingState.TRACKING &&
                                (imageName.contains("affe"))) {

                                Log.d("AR_DEBUG", "✅ Marker erkannt & platziert: ${image.name}")
                                placeApeOnImage(image)
                                return
                            }

                            if (image.trackingState == TrackingState.PAUSED) {
                                Log.d("AR_DEBUG", "⏸️ Marker erkannt, aber noch nicht stabil.")
                            }
                        }
                    }

                    attempt++
                    if (attempt < maxAttempts) {
                        handler.postDelayed(this, 300)  // alle 300 ms neu prüfen
                    } else {
                        Log.d("AR_DEBUG", "❌ Marker wurde nach $maxAttempts Versuchen nicht erkannt.")
                    }

                } catch (e: Exception) {
                    Log.e("AR_DEBUG", "🚨 Fehler beim Marker-Scan: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        handler.post(scanRunnable)
    }




    private fun resetScene() {
        sceneView.children.filter { it !is CameraNode }.forEach {
            sceneView.removeChild(it)
        }
        apeAlreadyPlaced = false
        speechBubble.visibility = View.GONE
        tts.stop()
    }

    private fun placeModel() {
        modelNode.anchor()

        val firstText = "Hallo! Ich bin Polly. Ich zeige dir heute etwas Spannendes!"
        speechBubble.text = firstText
        speechBubble.visibility = View.VISIBLE
        tts.speak(firstText, TextToSpeech.QUEUE_FLUSH, null, null)

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
    }

    private fun replaceModelWithFlyingPolly() {
        sceneView.removeChild(modelNode)

        val monkeyNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync("models/parrot.glb", scaleToUnits = 3f) {
                sceneView.planeRenderer.isVisible = true
            }
        }

        sceneView.addChild(monkeyNode)
    }

    private fun placeApeOnImage(image: AugmentedImage) {
        Log.d("AR_DEBUG", "placeApeOnImage aufgerufen für ${image.name}")
        if (apeAlreadyPlaced) return

        val anchor = image.createAnchor(image.centerPose)

        val monkeyNode = ArModelNode(sceneView.engine).apply {
            this.anchor = anchor
            loadModelGlbAsync("models/monkey.glb", scaleToUnits = 0.5f)
            position = Position(y = 0.05f)
        }

        sceneView.addChild(monkeyNode)
        apeAlreadyPlaced = true

        Toast.makeText(this, "Affe gefunden!", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
