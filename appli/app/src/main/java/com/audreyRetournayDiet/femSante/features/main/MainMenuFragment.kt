package com.audreyRetournayDiet.femSante.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.recommendation.Recommendation
import com.audreyRetournayDiet.femSante.features.alim.AlimActivity
import com.audreyRetournayDiet.femSante.features.calendar.add.EntryAddActivity
import com.audreyRetournayDiet.femSante.features.calendar.view.CalendarActivity
import com.audreyRetournayDiet.femSante.features.corps.BienCorpsActivity
import com.audreyRetournayDiet.femSante.features.tete.BienTeteActivity
import com.audreyRetournayDiet.femSante.features.ToolboxActivity
import com.audreyRetournayDiet.femSante.shared.RecommendationLauncher
import com.audreyRetournayDiet.femSante.viewModels.main.PourToiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
@AndroidEntryPoint
class MainMenuFragment : Fragment() {

    companion object {
        private const val MAX_RECOMMENDATIONS_DISPLAYED = 5
    }

    private lateinit var tete: Button
    private lateinit var corps: Button
    private lateinit var outils: Button
    private lateinit var alim : Button
    private lateinit var calendarActivity: Button

    private lateinit var bannerPourToi: TextView
    private lateinit var containerRecommendations: ViewGroup

    // Hilt fournit la Factory : plus de plomberie manuelle (DatabaseProvider, Repository, UserStore).
    private val pourToiViewModel: PourToiViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView: Affichage du menu principal")
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        initViews(view)
        setupListeners()
        observePourToi()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Recharge les recommandations au retour sur le menu (ex. après une saisie de
        // journal dans le calendrier) pour que "Pour toi aujourd'hui" reste à jour.
        pourToiViewModel.refresh()
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
        bannerPourToi = view.findViewById(R.id.textPourToiBanner)
        containerRecommendations = view.findViewById(R.id.containerRecommendations)
    }

    /**
     * Observe la section additive "Pour toi aujourd'hui" : bandeau incitatif si pas de
     * saisie du jour, liste de contenu recommandé sinon. Ne bloque jamais le reste du
     * menu, qui reste accessible quel que soit l'état du journal.
     */
    private fun observePourToi() {
        bannerPourToi.setOnClickListener {
            Timber.i("Navigation: Vers le journal (EntryAddActivity) depuis le bandeau incitatif")
            startActivity(Intent(activity, EntryAddActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pourToiViewModel.uiState.collect { state ->
                    if (state.isLoading) return@collect

                    bannerPourToi.visibility = if (state.hasEntryToday) View.GONE else View.VISIBLE
                    renderRecommendations(state.recommendations.take(MAX_RECOMMENDATIONS_DISPLAYED))
                }
            }
        }
    }

    /**
     * Construit dynamiquement la liste des badges "Recommandé pour toi" à partir du
     * layout réutilisable [R.layout.item_recommendation]. Affichage uniquement — le
     * clic n'ouvre pas encore le contenu visé, qui reste accessible via les menus
     * existants (Alimentation, Bien-être, Boîte à outils).
     */
    private fun renderRecommendations(recommendations: List<Recommendation>) {
        containerRecommendations.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        recommendations.forEach { recommendation ->
            val row = inflater.inflate(R.layout.item_recommendation, containerRecommendations, false)
            row.findViewById<TextView>(R.id.textContentTitle).text = humanizeContentId(recommendation.content.id)
            row.setOnClickListener {
                Timber.i("Ouverture du contenu recommandé : ${recommendation.content.type} / ${recommendation.content.id}")
                RecommendationLauncher.launch(requireContext(), recommendation.content)
            }
            containerRecommendations.addView(row)
        }
    }

    private fun humanizeContentId(id: String): String {
        return id.removeSuffix(".pdf").replace("_", " ").replaceFirstChar { it.uppercase() }
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