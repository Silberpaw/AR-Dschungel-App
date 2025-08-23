package com.xperiencelabs.arapp
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.TrackingState
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.CameraNode
import io.github.sceneview.renderable.Renderable
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView // hält die ARCore-Session, rendert Kamera & 3D-Content
    private lateinit var modelNode: ArModelNode // repräsentiert ein 3D-Modell
    private lateinit var speechBubble: TextView // zeigt den Text an
    private lateinit var tts: TextToSpeech // Text to Speech
    private lateinit var findApeButton: ExtendedFloatingActionButton // Button um Affen zu finden
    private lateinit var quizLauncher: ActivityResultLauncher<Intent> // startet QuizActivity

    private var arrowNode: ArModelNode? = null // zeigt einen Pfeil an
    private var arrowAnimator: ValueAnimator? = null // animiert den Pfeil

    private var apeAlreadyPlaced = false // um Affen nur einmal zu platzieren


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView)

        sceneView.configureSession { session, config ->
            try {
                val inputStream = assets.open("augmentedimages/marker_affe.imgdb") // Pfad zur Bilddatenbank
                val db = AugmentedImageDatabase.deserialize(session, inputStream) // Bilddatenbank laden
                config.augmentedImageDatabase = db // Bilddatenbank für Session
            } catch (e: Exception) { // Fehler beim Laden der Bilddatenbank
            }
        }
        quizLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) { // wenn Quiz erfolgreich beendet
                // Nach bestandenem Quiz: Button auf "Meine Sammlung" umstellen
                updateButtonToCollection() // Button anpassen
            }
        }
        speechBubble = findViewById(R.id.speechBubble) // zeigt den Text an

        tts = TextToSpeech(this) { // Text to Speech initialisieren
            if (it == TextToSpeech.SUCCESS) tts.language = Locale.GERMAN // Sprache einstellen
        }

        findApeButton = findViewById(R.id.btnFindApe) // Button um Affen zu finden
        findApeButton.visibility = View.GONE // Button ausblenden
        findApeButton.setOnClickListener { // Button Klick-Listener
            resetScene() // zurücksetzen
            checkOnceForApe() // Affen suchen
        }

        val startQuizButton = findViewById<ExtendedFloatingActionButton>(R.id.btnStartQuiz) // Button um Quiz zu starten
        startQuizButton.visibility = View.GONE   // erst zeigen, wenn Affe gefunden


        startIntroWithPolly() // Intro mit Polly starten
    }

    private fun startIntroWithPolly() {
        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply { // Modell laden
            loadModelGlbAsync("models/toon_parrot.glb", scaleToUnits = 0.7f) { // Modell laden
                rotation = Rotation(0f, 280f, 0f) // Modell drehen
                speakIntro() // Intro mit Polly sprechen
            }
            onTap = { motionEvent: MotionEvent, i: Renderable? ->
                runPollyTutorial() // Tutorial mit Polly
            }
        }
        sceneView.addChild(modelNode) // Modell in die Szene platzieren
    }

    private fun speakIntro() {
        val introText = "Hallo! Ich bin Polly. Ich bin hier um dir zu helfen. Klick mich an, um die Entdeckung zu starten!"
        speechBubble.text = introText // Text in Bubble setzen
        speechBubble.visibility = View.VISIBLE // Sprechblase Sichtbar
        tts.speak(introText, TextToSpeech.QUEUE_FLUSH, null, null) // Text mit Polly sprechen
    }

    private fun replacePollyWithFlyingVersion() {
        sceneView.removeChild(modelNode) // Modell aus Szene entfernen

        val flyingPollyNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply { // Flying Modell laden
            loadModelGlbAsync("models/parrot.glb", scaleToUnits = 3f) { // Flying Modell laden
                sceneView.planeRenderer.isVisible = true
            }
            position = Position(0f, 0f, -1f) // Position
        }

        sceneView.addChild(flyingPollyNode) // Flying Modell in Szene platzieren
    }
    private fun runPollyTutorial() {
        tts.stop() // Polly stoppen, falls Kind wiederholung will
        val firstText = "Lass uns das nächste Tier finden!"
        speechBubble.text = firstText // Sprachblase auf ersten text setzen
        tts.speak(firstText, TextToSpeech.QUEUE_FLUSH, null, null) // Sprachblase mit Polly sprechen

        Handler(Looper.getMainLooper()).postDelayed({
            val secondText = "Folg mir, um den Affen zu entdecken!"
            speechBubble.text = secondText // zweiter text
            tts.speak(secondText, TextToSpeech.QUEUE_FLUSH, null, null)

            Handler(Looper.getMainLooper()).postDelayed({
                val followText = "Hast du ein Symbol entdeckt? Dann klicke unten auf 'Tier finden'! und rufe es herbei."
                speechBubble.text = followText // dritter text
                tts.speak(followText, TextToSpeech.QUEUE_FLUSH, null, null)

                findApeButton.visibility = View.VISIBLE

                //  Polly durch fliegendes Modell ersetzen
                replacePollyWithFlyingVersion() // fliegendes Modell laden
                showArrow() // 3D-Pfeil anzeigen


            }, 7000) // delay damit natürlichere Pausen da sind
        }, 6000) // dito
    }

    private fun updateButtonToCollection() {
        val startQuizButton = findViewById<ExtendedFloatingActionButton>(R.id.btnStartQuiz)
        startQuizButton.text = "Meine Sammlung" // Button anpassen
        startQuizButton.setIconResource(R.drawable.baseline_pets_24)
        startQuizButton.setOnClickListener {
            startActivity(Intent(this, CollectionActivity::class.java))
        }
    }

    // Methode checkt op Affensymbol in Kamera gezeigt wird
    private fun checkOnceForApe() {

        startScanAnimation() // Animation starten
        val handler = Handler(Looper.getMainLooper())
        var attempt = 0 //versuch
        val maxAttempts = 20 //max versuche

        val img = findViewById<ImageView>(R.id.imgScanning)
        val scanHint = findViewById<TextView>(R.id.scanHint)

        img.visibility = View.VISIBLE
        scanHint.visibility = View.VISIBLE
        scanHint.text = "Ich suche nach dem Tiersymbol... Pass auf, dass das Bild gut sichtbar ist!"



        tts.speak("Zeig mir den Affen gut sichtbar in die Kamera!", TextToSpeech.QUEUE_FLUSH, null, null)


        val scanRunnable = object : Runnable {
            override fun run() {
                try {
                    val frame = sceneView.arSession?.update() ?: run {
                        handler.postDelayed(this, 300)
                        return
                    }

                    val images = frame.getUpdatedTrackables(AugmentedImage::class.java) // Bilder aus Frame holen

                    if (images.isEmpty()) { //wenn images leer sind
                        // nix
                    } else {
                        for (image in images) {
                            val imageName = image.name.lowercase()

                            if (image.trackingState == TrackingState.TRACKING && imageName.contains("affe")) {
                                placeApeOnImage(image) // platziert Affe auf augmented image anchor
                                return
                            }

                            if (image.trackingState == TrackingState.PAUSED) {
                            }
                        }
                    }

                    attempt++

                    if (attempt == 5) {
                        tts.speak(
                            "Vielleicht ist das Bild zu nah oder zu dunkel. Versuch es bitte noch einmal!",
                            TextToSpeech.QUEUE_ADD,
                            null,
                            null
                        )

                        // ⏸ Längere Pause: z. B. 3000ms (3 Sekunden)
                        handler.postDelayed(this, 5000)
                        return
                    }

                    if (attempt < maxAttempts) {
                        handler.postDelayed(this, 300)
                    } else {
                        // UI aufräumen
                        findViewById<ImageView>(R.id.imgScanning).visibility = View.GONE
                        findViewById<TextView>(R.id.scanHint).visibility = View.GONE
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        handler.post(scanRunnable)
        findViewById<ImageView>(R.id.imgScanning).visibility = View.GONE
        findViewById<TextView>(R.id.scanHint).visibility = View.GONE

    }

    private fun placeApeOnImage(image: AugmentedImage) {
        if (apeAlreadyPlaced) return // Affe schon platziert

        val anchor = image.createAnchor(image.centerPose) // anchor auf bild
        val monkeyNode = ArModelNode(sceneView.engine).apply { // platziert Affe
            this.anchor = anchor // anchor setzen
            loadModelGlbAsync("models/monkey.glb", scaleToUnits = 0.5f)
            position = Position(y = 0.05f)
        }

        sceneView.addChild(monkeyNode)
        apeAlreadyPlaced = true

        //  Tier in Sammlung speichern
        val prefs = getSharedPreferences("animal_collection", MODE_PRIVATE) // Tiercollection
        prefs.edit().putBoolean("affe", true).apply() // Tiercollection updaten
        val stars = prefs.getInt("stars", 0) // Sterne
        prefs.edit().putInt("stars", stars + 1).apply() // Sterne updaten

        // Polly & UI anpassen
        sceneView.removeChild(modelNode)
        speechBubble.visibility = View.GONE

        removeArrow()

        Toast.makeText(this, "Affe gefunden!", Toast.LENGTH_SHORT).show() // Toast
        val foundText = "Juhu! Du hast den Affen gefunden!"
        speechBubble.text = foundText
        speechBubble.visibility = View.VISIBLE
        tts.speak(foundText, TextToSpeech.QUEUE_FLUSH, null, null)


        val startQuizButton = findViewById<ExtendedFloatingActionButton>(R.id.btnStartQuiz) // Button um Quiz zu starten
        startQuizButton.text = "Quiz starten"
        startQuizButton.setIconResource(R.drawable.baseline_quiz_24)
        startQuizButton.setOnClickListener {
            launchQuiz()  // startet Quiz und wartet auf RESULT_OK
        }
        startQuizButton.visibility = View.VISIBLE // Button sichtbar machen

        findApeButton.visibility = View.GONE // Button ausblenden
        startQuizButton.visibility = View.VISIBLE // Button sichtbar machen
        findApeButton.visibility = View.GONE // Button ausblenden
        removeArrow() // Pfeil entfernen
        playSound(R.raw.monkeysound) // Affengeräusch abspielen
        Handler(Looper.getMainLooper()).postDelayed({
            speechBubble.text = "Schau mal in deine Sammlung, der Affe ist jetzt da!"
            tts.speak(speechBubble.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        }, 4000)

        val missionAnimal = prefs.getString("daily_mission", "papagei")
        if (image.name.contains(missionAnimal ?: "")) {
            Toast.makeText(this, "🎉 Mission erfüllt!", Toast.LENGTH_LONG).show()
            prefs.edit().putBoolean("mission_done", true).apply() // Mission erfüllt
        }

        val allAnimals = listOf("affe", "elefant", "pinguin", "löwe") // alle Tiere
        val collected = allAnimals.count { prefs.getBoolean(it, false) } // anzahl der ge收集en Tiere

        if (collected == 2 && !prefs.getBoolean("badge_tierprofi", false)) { // badge setzen
            prefs.edit().putBoolean("badge_tierprofi", true).apply() // badge setzen
            Toast.makeText(this, "🏅 Du bist jetzt Tierprofi!", Toast.LENGTH_LONG).show()
        }


    }
    // um Affengeräusch zu machen
    private fun playSound(resId: Int) {
        val mp = MediaPlayer.create(this, resId)
        mp.start()
        mp.setOnCompletionListener { it.release() }
    }

    // der affe ist für das Testen schon in der Sammlung. irrelavant da die reihenfolge wie die app
    // benutzt wird von testsituation vorgegeben ist
    private fun saveAnimalToCollection(animalId: String) {
        val prefs = getSharedPreferences("animal_collection", MODE_PRIVATE) // Tiercollection
        val editor = prefs.edit() // Tiercollection updaten
        editor.putBoolean(animalId, true) // Tiercollection updaten
        editor.apply() // Tiercollection updaten
    }

    private fun showArrow() {
        arrowNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply { // Pfeil laden
            loadModelGlbAsync("models/arrow.glb", scaleToUnits = 0.5f) {
                startArrowAnimation()  // Startet Animation sobald geladen
            }
            position = Position(x = 0.5f, y = 0f, z = -1.0f) // Position setzen
            rotation = Rotation(0f, 90f, 0f) // Drehung setzen
        }
        sceneView.addChild(arrowNode!!) // Pfeil in Szene platzieren
    }

    private fun startArrowAnimation() {
        arrowAnimator?.cancel() // falls vorherige Animation läuft

        arrowAnimator = ValueAnimator.ofFloat(-20f, 20f).apply {
            duration = 800
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener { animator ->
                val angle = animator.animatedValue as Float // Drehung
                arrowNode?.rotation = Rotation(0f, 90f + angle, 0f) // Drehung setzen
            }

            start() // Animation starten
        }
    }
    private fun startScanAnimation() {
        val img = findViewById<ImageView>(R.id.imgScanning)
        img.visibility = View.VISIBLE // Bild sichtbar machen
        val rotate = ObjectAnimator.ofFloat(img, View.ROTATION, 0f, 360f)
        rotate.duration = 4000
        rotate.repeatCount = ObjectAnimator.INFINITE
        rotate.start()
    }

    private fun removeArrow() {
        arrowAnimator?.cancel() // falls vorherige Animation läuft
        arrowAnimator = null // falls vorherige Animation läuft

        arrowNode?.let {
            sceneView.removeChild(it)
            arrowNode = null
        }
    }
    private fun launchQuiz() {
        val intent = Intent(this, QuizActivity::class.java) // QuizActivity starten
            .putExtra("animalId", "affe") // AnimalId setzen
        quizLauncher.launch(intent) // QuizActivity starten
    }
    private fun resetScene() {
        sceneView.children.filter { it !is CameraNode }.forEach { // alle Elemente aus Szene entfernen
            sceneView.removeChild(it) // alle Elemente aus Szene entfernen
        }
        apeAlreadyPlaced = false // Affe nicht platziert
        speechBubble.visibility = View.GONE // Sprechblase ausblenden
        tts.stop() // Polly stoppen
        removeArrow() // Pfeil entfernen
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown() // Polly beenden
    }
}
