package com.audreyRetournayDiet.femSante.domain.alim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R

class AlimFragment : Fragment() {

    private lateinit var breakfeast : Button
    private lateinit var entry : Button
    private lateinit var plats : Button
    private lateinit var dessert : Button
    private val map : HashMap<String, String> = HashMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_alim, container, false)

        entry = view.findViewById(R.id.buttonEntry)
        breakfeast = view.findViewById(R.id.buttonBreakfirst)
        plats = view.findViewById(R.id.buttonPlat)
        dessert = view.findViewById(R.id.buttonEBook)

        val intentTarget = Intent(activity, RecetteActivity::class.java)

        entry.setOnClickListener {
            map["ent1"] = "Salade d’été"
            map["ent2"] = "Tartines gourmandes au thon"
            map["ent3"] = "Velouté d’épinards, amandes et noisettes"
            map["ent4"] = "Blinis avocat saumon"
            map["ent5"] = "Houmous de betteraves"
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
            map["plat3"] = "Cake salé"
            map["plat4"] = "Polenta aux champignons"
            map["plat5"] = "Tofu fumé - Purée de carottes et panais"
            map["plat6"] = "Tarte aux épinards"
            recetteActivityLaunch("Plats", map, intentTarget)
        }

        dessert.setOnClickListener {
            map["des1"] = "Petits cakes vapeur pommes-myrtilles"
            map["des2"] = "Crème au chocolat"
            map["des3"] = "Fondant au chocolat"
            map["des4"] = "Tartelettes à la pomme express"
            map["des5"] = "Cake au chocolat léger et sans farines"
            map["des6"] = "Carrés gourmands"
            map["des7"] = "Mini cake moelleux banane et chocolat"
            recetteActivityLaunch("Desserts", map, intentTarget)
        }

        return view
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