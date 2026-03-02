package com.audreyRetournayDiet.femSante.features.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber

/**
 * Activité principale de l'application (Dashboard).
 * * Cette activité accueille l'utilisatrice après sa connexion ou si sa session est active.
 * Elle gère :
 * 1. **La navigation de base** : Switch entre [MainMenuFragment] et [AccountFragment].
 * 2. **L'accueil personnalisé** : Affichage d'un message de bienvenue unique.
 * 3. **La persistance de l'état** : Évite la recréation inutile des fragments lors des rotations.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var menu: BottomNavigationView

    /**
     * Instances des fragments maintenues pour la durée de vie de la session de l'activité.
     * Cela permet de conserver l'état de défilement ou de saisie lors du switch d'onglet.
     */
    private val homeFragment = MainMenuFragment()
    private val accountFragment = AccountFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Timber.d("onCreate: Chargement de l'écran d'accueil")

        handleWelcomeMessage()
        initNavigation(savedInstanceState)
    }

    /**
     * Traite les informations de l'Intent pour afficher un toast de bienvenue.
     * Le flag "SHOW_WELCOME_MESSAGE" est supprimé après affichage pour éviter
     * la réapparition du toast lors d'un changement de configuration (ex: rotation).
     */
    private fun handleWelcomeMessage() {
        val showWelcome = intent.getBooleanExtra("SHOW_WELCOME_MESSAGE", false)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "Utilisatrice"

        if (showWelcome) {
            Timber.i("Affichage du message de bienvenue pour : $userEmail")
            Toast.makeText(this, "Connectée en tant que : $userEmail", Toast.LENGTH_LONG).show()

            // Consommation de l'extra pour garantir l'unicité de l'affichage
            intent.removeExtra("SHOW_WELCOME_MESSAGE")
        }
    }

    /**
     * Initialise la BottomNavigationView et le fragment par défaut.
     * * @param savedInstanceState Si null, on charge le fragment initial.
     */
    private fun initNavigation(savedInstanceState: Bundle?) {
        menu = findViewById(R.id.bottom_navigation_menu)

        // Affichage du fragment initial uniquement au premier lancement
        if (savedInstanceState == null) {
            Timber.d("Initialisation : Affichage du fragment Home par défaut")
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, homeFragment)
                .commit()
        }

        setupNavigationListener()
    }

    /**
     * Configure le listener sur la barre de navigation.
     * Vérifie l'instance du fragment actuel avant de remplacer pour éviter des transactions inutiles.
     */
    private fun setupNavigationListener() {
        menu.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

            when (item.itemId) {
                R.id.menu -> {
                    if (currentFragment !is MainMenuFragment) {
                        Timber.v("Navigation : Switch vers MENU")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, homeFragment)
                            .commit()
                    }
                    true
                }
                R.id.account -> {
                    if (currentFragment !is AccountFragment) {
                        Timber.v("Navigation : Switch vers COMPTE")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, accountFragment)
                            .commit()
                    }
                    true
                }
                else -> {
                    Timber.w("Navigation : ID de menu inconnu (${item.itemId})")
                    false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.v("onResume: L'activité est de nouveau visible")
    }
}