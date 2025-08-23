package com.xperiencelabs.arapp

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.PixelCopy
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ArSelfieActivity : AppCompatActivity() {

    private lateinit var arView: ArSceneView
    private lateinit var modelPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_selfie)

        // SceneView
        arView = findViewById(R.id.sceneView)

        // Modell laden
        modelPath = intent.getStringExtra("model") ?: run {
            finish()
            return
        }
        // Modell laden Toast
        Toast.makeText(this, "Lade Modell:\n$modelPath", Toast.LENGTH_SHORT).show()


        // wenn asset nicht existiert dann beenden
        if (!assetExists(modelPath)) {
            return
        }

        arView.onArSessionCreated = { session ->
            arView.configureSession { _, config ->
                config.focusMode = com.google.ar.core.Config.FocusMode.AUTO
                config.instantPlacementMode =
                    com.google.ar.core.Config.InstantPlacementMode.LOCAL_Y_UP
            }

            arView.planeRenderer.isVisible = true

            // Modell vom Affen rein
            placeAnimalModel(arView, modelPath, scale = 1.2f, yOffset = 0f)
        }

        // Screenshot button festlegen und methode aufrufen
        findViewById<ExtendedFloatingActionButton>(R.id.btnScreenshot).setOnClickListener {
            //takeScreenshot()
        }
    }

    // platziert Modell
    private fun placeAnimalModel(
        sceneView: ArSceneView,
        modelPath: String,
        scale: Float = 1.2f, // Größe
        yOffset: Float = 0f // Drehen
    ): ArModelNode {
        val node = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(modelPath, scaleToUnits = scale, autoAnimate = true) {
                // Wenn geladen: Position vor die Kamera setzen
                position = Position(0f, yOffset, -1.2f)
            }
        }
        sceneView.addChild(node)
        return node
    }

    /*// Screen
    private fun takeScreenshot() {
        val bitmap = Bitmap.createBitmap(arView.width, arView.height, Bitmap.Config.ARGB_8888)
        val handler = Handler(Looper.getMainLooper())
        PixelCopy.request(arView, bitmap, { result ->
            if (result == PixelCopy.SUCCESS) saveBitmapToGallery(bitmap)
        }, handler)
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {  }
*/
    // Hilfsfunktion: prüft, ob Asset existiert
    private fun assetExists(path: String): Boolean {
        return try {
            val dir = path.substringBeforeLast("/", "")
            val file = path.substringAfterLast("/")
            val list = assets.list(dir.ifEmpty { "" })?.toList() ?: emptyList()
            Log.d("AR_SELFIE", "Assets in '$dir': $list")
            list.contains(file)
        } catch (e: Exception) {
            Log.e("AR_SELFIE", "Fehler: ${e.message}")
            false
        }
    }


}
