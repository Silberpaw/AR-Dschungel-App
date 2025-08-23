package com.xperiencelabs.arapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {

    private var currentQuestion = 0 // bei welcher Frage das Kind grade ist
    private var correctAnswers = 0 // damit man überprüft ob das Kind eine oder zwei Fragen gewusst hat

    private val questions = listOf(
        QuizQuestion( // QuizQuestion ist eine Datenklasse die aus einem Text, einer Liste an antworten und einem Index besteht
            "Wo leben Affen frei?",
            listOf("Europa", "Afrika und Asien", "Australien", "Antarktis"), // Afrika und Asien
            correctIndex = 1 // Index 1 weil Afrika
        ),
        QuizQuestion(
            "Was gehört zum Lieblingsessen eines Affen?", // Frage
            listOf("Bananen", "Fisch", "Mais", "Hühnchen"), // Bananen
            correctIndex = 0 // Bananen ist korrekt alos ist der Index 0
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        showQuestion() // ruft Methode auf

        // die Buttons werden gesetzt und je nachdem welcher Button gedrückt wird, wird
        // der Index des Buttons der check Answer übergeben
        findViewById<Button>(R.id.answer1).setOnClickListener { checkAnswer(0) }
        findViewById<Button>(R.id.answer2).setOnClickListener { checkAnswer(1) }
        findViewById<Button>(R.id.answer3).setOnClickListener { checkAnswer(2) }
        findViewById<Button>(R.id.answer4).setOnClickListener { checkAnswer(3) }
    }

    // zeigt die nächste Frage
    private fun showQuestion() {
        val q = questions[currentQuestion]
        findViewById<TextView>(R.id.quizQuestion).text = q.text
        findViewById<Button>(R.id.answer1).text = q.answers[0]
        findViewById<Button>(R.id.answer2).text = q.answers[1]
        findViewById<Button>(R.id.answer3).text = q.answers[2]
        findViewById<Button>(R.id.answer4).text = q.answers[3]
    }

    // überprüft ob die Antwort richtig ist
    private fun checkAnswer(selectedIndex: Int) {
        val q = questions[currentQuestion] // q ist die aktuelle Frage
        if (selectedIndex == q.correctIndex) {
            Toast.makeText(this, "Richtig!", Toast.LENGTH_SHORT).show()
            correctAnswers++ // correctAnswers wird erhöht
        } else {
            Toast.makeText(this, "Falsch!", Toast.LENGTH_SHORT).show()
        }

        // damit die nächste Frage aufgerufen wird
        if (currentQuestion < questions.lastIndex) {
            currentQuestion++ // nächste Frage
            showQuestion() //nochmal in die ShowQuestion Methode
        } else {
            // Quiz Ende
            if (correctAnswers == questions.size) {
                Toast.makeText(this, "Supi! Du hast alle Fragen richtig beantwortet!", Toast.LENGTH_LONG).show()
                setResult(RESULT_OK) // Zurück zur Main Activity
            } else {
                Toast.makeText(this, "Du hast ${correctAnswers} von ${questions.size} richtig gemacht.", Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
            }
            finish()
        }
    }
}

data class QuizQuestion(
    val text: String,
    val answers: List<String>,
    val correctIndex: Int
)