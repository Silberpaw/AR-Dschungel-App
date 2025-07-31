package com.xperiencelabs.arapp

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isGone
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config

// Sceneview Imports
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.material.setExternalTexture
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.VideoNode

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

        // Initialisiert die Videowiedergabe
        mediaPlayer = MediaPlayer.create(this,R.raw.ad)

        // Initialisiert den Button für das Platzieren des Modells
        placeButton = findViewById(R.id.place)

        // Setzt einen Klick-Listener auf Button
        placeButton.setOnClickListener {
            // Platziert das Modell
            placeModel()
        }
        // Initialisiert das Video-Modell
        videoNode = VideoNode(sceneView.engine, scaleToUnits = 0.7f, centerOrigin = Position(y=-4f), glbFileLocation = "models/plane.glb", player = mediaPlayer, onLoaded = {_,_ ->
            // Startet die Videowiedergabe
            mediaPlayer.start()
        })

        // Initialisiert das 3D-Modell
        modelNode = ArModelNode(sceneView.engine,PlacementMode.INSTANT).apply {
            // Platziert das Modell in der Scene
            loadModelGlbAsync(
                glbFileLocation = "models/toon_parrot.glb",
                scaleToUnits = 0.7f, //3 für parrot und monkey, 0.7 für toon monkey
                //centerOrigin = Position(-0.5f)

            )
            {
                // Setzt die Farbe des Modells
                sceneView.planeRenderer.isVisible = true
                val materialInstance = it.materialInstances[0]
            }
            // Setzt einen Klick-Listener auf das Modell
            onAnchorChanged = {
                placeButton.isGone = it != null
            }

        }
        // Fügt das Modell zur Scene hinzu
        sceneView.addChild(modelNode)
        // Fügt das Video-Modell zur Scene hinzu
        modelNode.addChild(videoNode)

    }

    // Platziert das Modell
   private fun placeModel(){
       // Platziert das Modell in der Scene
       modelNode.anchor()
        // Setzt die Farbe des Modells
       sceneView.planeRenderer.isVisible = false

   }


    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

}