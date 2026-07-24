package com.audreyRetournayDiet.femSante.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.recommendation.Recommendation
import com.audreyRetournayDiet.femSante.features.calendar.add.EntryAddActivity
import com.audreyRetournayDiet.femSante.shared.RecommendationLauncher
import com.audreyRetournayDiet.femSante.viewModels.main.PourToiViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Onglet "Pour toi" (accueil par défaut) : l'espace personnalisé.
 *
 * - **Ton geste du jour** : une seule action douce mise en avant (la recommandation la
 *   mieux notée), pour ne pas noyer sous un mur de contenu (ton "zéro anxiété").
 * - Le reste des recommandations juste en dessous.
 * - Sans saisie du jour : bandeau incitatif à remplir le journal.
 *
 * La bibliothèque de contenus (nutrition, bien-être, outils) vit dans [ExploreFragment].
 */
@AndroidEntryPoint
class PourToiFragment : Fragment() {

    companion object {
        private const val MAX_RECOMMENDATIONS_DISPLAYED = 5
    }

    private lateinit var cardGeste: MaterialCardView
    private lateinit var textGesteTitle: TextView
    private lateinit var buttonGesteAction: MaterialButton
    private lateinit var bannerPourToi: TextView
    private lateinit var textRecoMoreTitle: TextView
    private lateinit var containerRecommendations: ViewGroup
    private lateinit var layoutEmpty: View

    private val pourToiViewModel: PourToiViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_pour_toi, container, false)
        initViews(view)
        observePourToi()
        return view
    }

    override fun onResume() {
        super.onResume()
        // Couvre le retour d'une activité (ex. EntryAddActivity lancée depuis le bandeau).
        pourToiViewModel.refresh()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // Retour sur l'onglet "Pour toi" (navigation hide/show) : re-lecture de la saisie du jour.
        if (!hidden) pourToiViewModel.refresh()
    }

    private fun initViews(view: View) {
        cardGeste = view.findViewById(R.id.cardGesteDuJour)
        textGesteTitle = view.findViewById(R.id.textGesteTitle)
        buttonGesteAction = view.findViewById(R.id.buttonGesteAction)
        bannerPourToi = view.findViewById(R.id.textPourToiBanner)
        textRecoMoreTitle = view.findViewById(R.id.textRecoMoreTitle)
        containerRecommendations = view.findViewById(R.id.containerRecommendations)
        layoutEmpty = view.findViewById(R.id.layoutPourToiEmpty)

        bannerPourToi.setOnClickListener {
            Timber.i("Navigation: Vers le journal (EntryAddActivity) depuis le bandeau incitatif")
            startActivity(Intent(activity, EntryAddActivity::class.java))
        }
    }

    private fun observePourToi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pourToiViewModel.uiState.collect { state ->
                    if (state.isLoading) return@collect

                    bannerPourToi.isVisible = !state.hasEntryToday
                    bindGesteDuJour(state.recommendations.firstOrNull())
                    // Le reste, hors "geste du jour", dans le carrousel.
                    renderRecommendations(state.recommendations.drop(1).take(MAX_RECOMMENDATIONS_DISPLAYED))
                    // Rien à proposer : message doux plutôt qu'un écran vide.
                    layoutEmpty.isVisible = state.recommendations.isEmpty()
                }
            }
        }
    }

    /** Met en avant la recommandation la mieux notée comme "geste du jour". */
    private fun bindGesteDuJour(top: Recommendation?) {
        if (top == null) {
            cardGeste.isVisible = false
            return
        }
        cardGeste.isVisible = true
        textGesteTitle.text = humanizeContentId(top.content.id)
        buttonGesteAction.setOnClickListener {
            Timber.i("Geste du jour ouvert : ${top.content.type} / ${top.content.id}")
            RecommendationLauncher.launch(requireContext(), top.content)
        }
    }

    private fun renderRecommendations(recommendations: List<Recommendation>) {
        textRecoMoreTitle.isVisible = recommendations.isNotEmpty()
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
}
