package com.xperiencelabs.arapp

// Datenklasse Animal. Beinhaltet alle angezeigten Infos für die Infokarte, sowie ein string der auf das dazugehörige 3d-Modell
// verweist

data class Animal(
    val name: String,
    val origin: String,
    val age: String,
    val food: String,
    val funFact: String,
    val imageResId: Int,
    val model: String
)
