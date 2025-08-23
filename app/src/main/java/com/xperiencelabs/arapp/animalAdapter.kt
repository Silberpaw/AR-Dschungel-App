package com.xperiencelabs.arapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView


class AnimalAdapter(private val context: Context, private val animals: List<Animal>) : BaseAdapter() {

    // Anzahl der Elemente in der Liste

    override fun getCount(): Int = animals.size

    // Liefert das Element an Position; Rückgabetyp Any wegen BaseAdapter-Signatur

    override fun getItem(position: Int): Any = animals[position]

    // Eindeutige ID pro Zeile; hier einfach die Position als Long

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.animal, parent, false)

        // Daten für die aktuelle Zeile

        val animal = animals[position]

        // Selfie-Button holen und Klick-Listener setzen

        val selfieButton = view.findViewById<ImageButton>(R.id.btnSelfie)
        selfieButton.setOnClickListener {

            // Startet die AR-Activity und übergibt relevante Daten

            val intent = Intent(context, ArSelfieActivity::class.java)
            intent.putExtra("animalName", animal.name)
            intent.putExtra("model",
                animal.model)
            context.startActivity(intent)
        }

        // Views befüllen (Bild, Name, Details)

        view.findViewById<ImageView>(R.id.imageAnimal).setImageResource(animal.imageResId)
        view.findViewById<TextView>(R.id.textName).text = animal.name
        view.findViewById<TextView>(R.id.textDetails).text = """
            Herkunft: ${animal.origin}
            Alter: ${animal.age}
            Essen: ${animal.food}
            Funfact: ${animal.funFact}
        """.trimIndent()

        return view
    }
}
