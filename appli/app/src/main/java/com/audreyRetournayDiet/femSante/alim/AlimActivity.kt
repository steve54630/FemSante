package com.audreyRetournayDiet.femSante.alim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.PdfActivity

class AlimActivity : AppCompatActivity() {

    private lateinit var breakfeast : Button
    private lateinit var entry : Button
    private lateinit var plats : Button
    private lateinit var dessert : Button
    private lateinit var ebook : Button
    private val map : HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alim)

        entry = findViewById(R.id.buttonEntry)
        breakfeast = findViewById(R.id.buttonBreakfirst)
        plats = findViewById(R.id.buttonPlat)
        dessert = findViewById(R.id.buttonDessert)
        ebook = findViewById(R.id.buttonEbook)

        val intentTarget = Intent(this, RecetteActivity::class.java)

        entry.setOnClickListener {
            map["ent1"] = "Salade d’été"
            map["ent2"] = "Tartines gourmandes au thon"
            map["ent3"] = "Velouté d’épinards, amandes et noisettes"
            recetteActivityLaunch("Entrée", map, intentTarget)
        }

        breakfeast.setOnClickListener {
            map["bf1"] = "Pain de lentilles corail et oeufs brouillés"
            map["bf2"] = "Porridge salé aux amandes"
            recetteActivityLaunch("Petit-déjeuner", map, intentTarget)
        }

        plats.setOnClickListener {
            map["plat1"] = "Crêpes salées au houmous rose"
            map["plat2"] = "Tartelettes saumon et champignons"
            recetteActivityLaunch("Plats", map, intentTarget)
        }

        dessert.setOnClickListener {
            map["des1"] = "Petits cakes vapeur pommes-myrtilles"
            map["des2"] = "Crème au chocolat"
            map["des3"] = "Fondant au chocolat"
            map["des4"] = "Tartelettes à la pomme express"
            recetteActivityLaunch("Desserts", map, intentTarget)
        }

        ebook.setOnClickListener{
            val intent = Intent(this, PdfActivity::class.java)
            intent.putExtra("PDF", "ebook_nutrition.pdf")
            startActivity(intent)
        }
    }

    private fun recetteActivityLaunch(titre: String?, map: HashMap<String, String>, intentTarget: Intent) {
        intentTarget.putExtra("Title", titre)
        intentTarget.putExtra("map", map)
        startActivity(intentTarget)
    }

    override fun onResume() {
        map.clear()
        super.onResume()
    }
}