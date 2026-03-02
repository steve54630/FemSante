package com.audreyRetournayDiet.femSante.features.alim

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlimActivity : AppCompatActivity() {

    private val tag = "ACT_ALIM"
    private lateinit var menu: BottomNavigationView

    // On conserve les instances pour éviter de recréer les fragments à chaque clic
    private var alimFragment = AlimFragment()
    private var docFragment = RessourceFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alim)

        Log.d(tag, "Lancement de AlimActivity")

        menu = findViewById(R.id.bottom_navigation_menu)

        // Affichage du fragment par défaut au démarrage
        if (savedInstanceState == null) {
            Log.i(tag, "Premier lancement : affichage du fragment Alim")
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, alimFragment)
                .commit()
        }

        menu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.alim -> {
                    Log.d(tag, "Navigation : Onglet Alimentation sélectionné")
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, alimFragment)
                        .commit()
                    true
                }
                R.id.pdf -> {
                    Log.d(tag, "Navigation : Onglet Ressources PDF sélectionné")
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, docFragment)
                        .commit()
                    true
                }
                else -> {
                    Log.w(tag, "Navigation : Item de menu inconnu cliqué (ID: ${item.itemId})")
                    false
                }
            }
        }
    }
}