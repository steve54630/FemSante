package com.audreyRetournayDiet.femSante.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.features.ToolboxActivity
import com.audreyRetournayDiet.femSante.features.alim.AlimActivity
import com.audreyRetournayDiet.femSante.features.corps.BienCorpsActivity
import com.audreyRetournayDiet.femSante.features.tete.BienTeteActivity
import timber.log.Timber

/**
 * Onglet "Découvrir" : la bibliothèque de contenus (nutrition, bien-être mental et
 * physique, boîte à outils). Séparé de "Pour toi" pour distinguer clairement l'espace
 * personnalisé de l'espace d'exploration.
 */
class ExploreFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        setupListeners(view)
        return view
    }

    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.buttonAlim).setOnClickListener {
            Timber.i("Navigation: Vers Alimentation (AlimActivity)")
            startActivity(Intent(activity, AlimActivity::class.java))
        }
        view.findViewById<Button>(R.id.buttonTete).setOnClickListener {
            Timber.i("Navigation: Vers Bien-être Mental (BienTeteActivity)")
            startActivity(Intent(activity, BienTeteActivity::class.java))
        }
        view.findViewById<Button>(R.id.buttonCorps).setOnClickListener {
            Timber.i("Navigation: Vers Bien-être Physique (BienCorpsActivity)")
            startActivity(Intent(activity, BienCorpsActivity::class.java))
        }
        view.findViewById<Button>(R.id.buttonOutils).setOnClickListener {
            Timber.i("Navigation: Vers Boîte à Outils (ToolboxActivity)")
            startActivity(Intent(activity, ToolboxActivity::class.java))
        }
    }
}
