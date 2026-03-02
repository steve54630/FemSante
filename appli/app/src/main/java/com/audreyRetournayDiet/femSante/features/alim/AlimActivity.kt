package com.audreyRetournayDiet.femSante.features.alim

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber

/**
 * Activité principale du module "Bien dans son Assiette".
 * * Cette activité gère la navigation entre les conseils nutritionnels (AlimFragment)
 * et la bibliothèque de ressources PDF (RessourceFragment) via une barre de navigation basse.
 * * @property alimFragment Instance persistante pour la vue des conseils nutritionnels.
 * @property docFragment Instance persistante pour la consultation des documents PDF.
 */
class AlimActivity : AppCompatActivity() {

    private lateinit var menu: BottomNavigationView

    private var alimFragment = AlimFragment()
    private var docFragment = RessourceFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alim)

        Timber.d("Lancement de AlimActivity")

        menu = findViewById(R.id.bottom_navigation_menu)

        if (savedInstanceState == null) {
            Timber.i("Initialisation : affichage du fragment Alim par défaut")
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, alimFragment)
                .commit()
        }

        setupNavigation()
    }

    /**
     * Configure le listener de la [BottomNavigationView].
     * Assure la permutation entre les fragments du module Alimentation.
     */
    private fun setupNavigation() {
        menu.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.alim -> {
                    Timber.d("Navigation : Onglet Alimentation")
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, alimFragment)
                        .commit()
                    true
                }
                R.id.pdf -> {
                    Timber.d("Navigation : Onglet Ressources PDF")
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, docFragment)
                        .commit()
                    true
                }
                else -> {
                    Timber.w("Navigation : ID inconnu cliqué -> ${item.itemId}")
                    false
                }
            }
        }
    }
}