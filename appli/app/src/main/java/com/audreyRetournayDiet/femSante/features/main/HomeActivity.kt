package com.audreyRetournayDiet.femSante.features.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Activité principale de l'application (Dashboard).
 * * Cette activité accueille l'utilisatrice après sa connexion ou si sa session est active.
 * Elle gère :
 * 1. **La navigation de base** : 4 onglets (Pour toi, Découvrir, Calendrier, Compte) en add/hide/show.
 * 2. **L'accueil personnalisé** : Affichage d'un message de bienvenue unique.
 * 3. **La persistance de l'état** : Évite la recréation inutile des fragments lors des rotations.
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var menu: BottomNavigationView

    /**
     * Instances des fragments maintenues pour la durée de vie de la session de l'activité.
     * Cela permet de conserver l'état de défilement ou de saisie lors du switch d'onglet.
     */
    private var pourToiFragment = PourToiFragment()
    private var exploreFragment = ExploreFragment()
    private var calendarFragment = com.audreyRetournayDiet.femSante.features.calendar.view.CalendarFragment()
    private var accountFragment = AccountFragment()
    private lateinit var activeFragment: androidx.fragment.app.Fragment

    private companion object {
        const val TAG_POUR_TOI = "pour_toi"
        const val TAG_DECOUVRIR = "decouvrir"
        const val TAG_CALENDAR = "calendar"
        const val TAG_ACCOUNT = "account"
    }

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

            intent.removeExtra("SHOW_WELCOME_MESSAGE")
        }
    }

    private fun initNavigation(savedInstanceState: Bundle?) {
        menu = findViewById(R.id.bottom_navigation_menu)

        if (savedInstanceState == null) {
            Timber.d("Initialisation : onglet \"Pour toi\" par défaut")
            supportFragmentManager.beginTransaction()
                .add(R.id.container, pourToiFragment, TAG_POUR_TOI)
                .commit()
            activeFragment = pourToiFragment
        } else {
            // Restauration : récupérer les instances recréées par le FragmentManager.
            (supportFragmentManager.findFragmentByTag(TAG_POUR_TOI) as? PourToiFragment)?.let { pourToiFragment = it }
            (supportFragmentManager.findFragmentByTag(TAG_DECOUVRIR) as? ExploreFragment)?.let { exploreFragment = it }
            (supportFragmentManager.findFragmentByTag(TAG_CALENDAR) as? com.audreyRetournayDiet.femSante.features.calendar.view.CalendarFragment)?.let { calendarFragment = it }
            (supportFragmentManager.findFragmentByTag(TAG_ACCOUNT) as? AccountFragment)?.let { accountFragment = it }
            activeFragment = supportFragmentManager.fragments.lastOrNull { !it.isHidden } ?: pourToiFragment
        }

        setupNavigationListener()
    }

    /**
     * Configure le listener sur la barre de navigation.
     * Vérifie l'instance du fragment actuel avant de remplacer pour éviter des transactions inutiles.
     */
    private fun setupNavigationListener() {
        menu.setOnItemSelectedListener { item ->
            val (target, tag) = when (item.itemId) {
                R.id.pour_toi -> pourToiFragment to TAG_POUR_TOI
                R.id.decouvrir -> exploreFragment to TAG_DECOUVRIR
                R.id.calendar -> calendarFragment to TAG_CALENDAR
                R.id.account -> accountFragment to TAG_ACCOUNT
                else -> {
                    Timber.w("Navigation : ID de menu inconnu (${item.itemId})")
                    return@setOnItemSelectedListener false
                }
            }
            showFragment(target, tag)
            true
        }
    }

    /**
     * Affiche l'onglet demandé sans détruire les autres (add/hide/show). L'état est conservé,
     * et surtout [androidx.fragment.app.Fragment.onHiddenChanged] se déclenche de façon fiable
     * au retour sur un onglet — ce que `replace()` sur instance réutilisée ne garantissait pas.
     * Les onglets sont ajoutés à la demande.
     */
    private fun showFragment(target: androidx.fragment.app.Fragment, tag: String) {
        if (target === activeFragment) return
        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment)
            if (target.isAdded) show(target) else add(R.id.container, target, tag)
        }.commit()
        activeFragment = target
    }

    override fun onResume() {
        super.onResume()
        Timber.v("onResume: L'activité est de nouveau visible")
    }
}