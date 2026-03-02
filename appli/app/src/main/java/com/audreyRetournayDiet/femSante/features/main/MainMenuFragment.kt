package com.audreyRetournayDiet.femSante.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.features.alim.AlimActivity
import com.audreyRetournayDiet.femSante.features.calendar.view.CalendarActivity
import com.audreyRetournayDiet.femSante.features.corps.BienCorpsActivity
import com.audreyRetournayDiet.femSante.features.tete.BienTeteActivity
import com.audreyRetournayDiet.femSante.features.ToolboxActivity
import timber.log.Timber

/**
 * Fragment faisant office de menu principal (Hub) de l'application.
 * * Ce composant présente les différentes catégories de contenu à l'utilisatrice :
 * - **Alimentation** : Conseils et suivis diététiques.
 * - **Bien-être Mental** : Exercices et ressources "Bien dans sa tête".
 * - **Bien-être Physique** : Activités et conseils "Bien dans son corps".
 * - **Boîte à outils** : Calculateurs et ressources pratiques.
 * - **Calendrier** : Suivi des cycles et des rendez-vous.
 *
 * Il assure la navigation vers les activités spécialisées de chaque module.
 */
class MainMenuFragment : Fragment() {

    private lateinit var tete: Button
    private lateinit var corps: Button
    private lateinit var outils: Button
    private lateinit var alim : Button
    private lateinit var calendarActivity: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView: Affichage du menu principal")
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        initViews(view)
        setupListeners()

        return view
    }

    /**
     * Initialise les références des boutons de navigation à partir du layout.
     */
    private fun initViews(view: View) {
        alim = view.findViewById(R.id.buttonAlim)
        tete = view.findViewById(R.id.buttonTete)
        corps = view.findViewById(R.id.buttonCorps)
        outils = view.findViewById(R.id.buttonOutils)
        calendarActivity = view.findViewById(R.id.buttonCalendar)
    }

    /**
     * Configure les écouteurs de clics pour chaque section du menu.
     * Chaque bouton lance l'activité correspondante au module choisi.
     */
    private fun setupListeners() {
        // --- Section Alimentation ---
        alim.setOnClickListener {
            Timber.i("Navigation: Vers Alimentation (AlimActivity)")
            startActivity(Intent(activity, AlimActivity::class.java))
        }

        // --- Section Bien-être Mental ---
        tete.setOnClickListener {
            Timber.i("Navigation: Vers Bien-être Mental (BienTeteActivity)")
            startActivity(Intent(activity, BienTeteActivity::class.java))
        }

        // --- Section Bien-être Physique ---
        corps.setOnClickListener {
            Timber.i("Navigation: Vers Bien-être Physique (BienCorpsActivity)")
            startActivity(Intent(activity, BienCorpsActivity::class.java))
        }

        // --- Section Boîte à Outils ---
        outils.setOnClickListener {
            Timber.i("Navigation: Vers Boîte à Outils (ToolboxActivity)")
            startActivity(Intent(activity, ToolboxActivity::class.java))
        }

        // --- Section Calendrier / Suivi ---
        calendarActivity.setOnClickListener {
            Timber.i("Navigation: Vers Calendrier (CalendarActivity)")
            startActivity(Intent(activity, CalendarActivity::class.java))
        }
    }
}