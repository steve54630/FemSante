package com.audreyRetournayDiet.femSante.features.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private val tag = "ACT_HOME"
    private lateinit var menu: BottomNavigationView

    // On utilise des instances uniques pour la session
    private val homeFragment = MainMenuFragment()
    private val accountFragment = AccountFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        Log.d(tag, "onCreate: Chargement de l'écran d'accueil")

        // 1. Gestion du message de bienvenue
        val showWelcome = intent.getBooleanExtra("SHOW_WELCOME_MESSAGE", false)
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "Utilisatrice"

        if (showWelcome) {
            Log.i(tag, "Affichage du message de bienvenue pour : $userEmail")
            Toast.makeText(this, "Connectée en tant que : $userEmail", Toast.LENGTH_LONG).show()
            // On consomme l'intent pour ne pas réafficher le toast en cas de rotation d'écran
            intent.removeExtra("SHOW_WELCOME_MESSAGE")
        }

        menu = findViewById(R.id.bottom_navigation_menu)

        // 2. Fragment par défaut au lancement
        if (savedInstanceState == null) {
            Log.d(tag, "Initialisation : Affichage du fragment Home par défaut")
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, homeFragment)
                .commit()
        }

        // 3. Listener de navigation
        menu.setOnItemSelectedListener { item ->
            val currentFragment = supportFragmentManager.findFragmentById(R.id.container)

            when (item.itemId) {
                R.id.menu -> {
                    if (currentFragment !is MainMenuFragment) {
                        Log.v(tag, "Navigation : Switch vers MENU")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, homeFragment)
                            .commit()
                    }
                    true
                }
                R.id.account -> {
                    if (currentFragment !is AccountFragment) {
                        Log.v(tag, "Navigation : Switch vers COMPTE")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.container, accountFragment)
                            .commit()
                    }
                    true
                }
                else -> {
                    Log.w(tag, "Navigation : ID de menu inconnu (${item.itemId})")
                    false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.v(tag, "onResume: L'activité est de nouveau visible")
    }
}