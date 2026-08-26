package com.audreyRetournayDiet.femSante.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase
import com.audreyRetournayDiet.femSante.data.micronutrient.Micronutrient
import com.audreyRetournayDiet.femSante.data.micronutrient.toMicronutrientPhaseLabel
import com.audreyRetournayDiet.femSante.data.recipe.Recipe
import com.audreyRetournayDiet.femSante.data.recipe.RecipeCategory
import com.audreyRetournayDiet.femSante.data.recipe.RecipeOfDay
import com.audreyRetournayDiet.femSante.data.recommendation.Recommendation
import com.audreyRetournayDiet.femSante.features.alim.MicronutrientDetailActivity
import com.audreyRetournayDiet.femSante.features.alim.RecetteDetailActivity
import com.audreyRetournayDiet.femSante.features.calendar.add.EntryAddActivity
import com.audreyRetournayDiet.femSante.features.login.PremiumUpsellActivity
import com.audreyRetournayDiet.femSante.shared.RecommendationLauncher
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.viewmodels.alim.RecipeDetailViewModel
import com.audreyRetournayDiet.femSante.viewmodels.main.PourToiViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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

    private lateinit var cardRecipeOfDay: MaterialCardView
    private lateinit var textRecipeTitle: TextView
    private lateinit var textRecipeMeta: TextView
    private lateinit var textRecipePhase: TextView
    private lateinit var buttonRecipeAction: MaterialButton
    private lateinit var cardPhaseMicronutrients: MaterialCardView
    private lateinit var textPhaseMicronutrientsLabel: TextView
    private lateinit var chipGroupPhaseMicronutrients: ChipGroup
    private lateinit var cardGeste: MaterialCardView
    private lateinit var textGesteTitle: TextView
    private lateinit var buttonGesteAction: MaterialButton
    private lateinit var bannerPourToi: View
    private lateinit var btnFillJournal: MaterialButton
    private lateinit var textRecoMoreTitle: TextView
    private lateinit var containerRecommendations: ViewGroup
    private lateinit var layoutEmpty: View
    private lateinit var progressPourToi: ProgressBar

    private var hasRenderedOnce = false

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
        cardRecipeOfDay = view.findViewById(R.id.cardRecipeOfDay)
        textRecipeTitle = view.findViewById(R.id.textRecipeTitle)
        textRecipeMeta = view.findViewById(R.id.textRecipeMeta)
        textRecipePhase = view.findViewById(R.id.textRecipePhase)
        buttonRecipeAction = view.findViewById(R.id.buttonRecipeAction)
        cardPhaseMicronutrients = view.findViewById(R.id.cardPhaseMicronutrients)
        textPhaseMicronutrientsLabel = view.findViewById(R.id.textPhaseMicronutrientsLabel)
        chipGroupPhaseMicronutrients = view.findViewById(R.id.chipGroupPhaseMicronutrients)
        cardGeste = view.findViewById(R.id.cardGesteDuJour)
        textGesteTitle = view.findViewById(R.id.textGesteTitle)
        buttonGesteAction = view.findViewById(R.id.buttonGesteAction)
        bannerPourToi = view.findViewById(R.id.layoutPourToiBanner)
        btnFillJournal = view.findViewById(R.id.btnFillJournal)
        textRecoMoreTitle = view.findViewById(R.id.textRecoMoreTitle)
        containerRecommendations = view.findViewById(R.id.containerRecommendations)
        layoutEmpty = view.findViewById(R.id.layoutPourToiEmpty)
        progressPourToi = view.findViewById(R.id.progressPourToi)

        btnFillJournal.setOnClickListener {
            Timber.i("Navigation: Vers le journal (EntryAddActivity) depuis le bandeau incitatif")
            startActivity(Intent(activity, EntryAddActivity::class.java))
        }
    }

    private fun observePourToi() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pourToiViewModel.uiState.collect { state ->
                    if (state.isLoading) {
                        // Spinner uniquement avant le tout premier rendu : les rafraîchissements
                        // suivants (onResume, retour d'onglet) gardent silencieusement le dernier état.
                        progressPourToi.isVisible = !hasRenderedOnce
                        return@collect
                    }
                    progressPourToi.isVisible = false
                    hasRenderedOnce = true

                    bannerPourToi.isVisible = !state.hasEntryToday
                    bindRecipeOfDay(state.recipeOfDay)
                    bindPhaseMicronutrients(state.phaseMicronutrients, state.currentPhase)
                    bindGesteDuJour(state.recommendations.firstOrNull())
                    renderRecommendations(state.recommendations.drop(1).take(MAX_RECOMMENDATIONS_DISPLAYED))
                    // Rien à proposer : message doux plutôt qu'un écran vide.
                    layoutEmpty.isVisible = state.recommendations.isEmpty()
                }
            }
        }
    }

    /** Affiche la recette du jour (selon la phase du cycle), ou masque la carte si indisponible. */
    private fun bindRecipeOfDay(recipeOfDay: RecipeOfDay?) {
        if (recipeOfDay == null) {
            cardRecipeOfDay.isVisible = false
            return
        }
        val recipe = recipeOfDay.recipe
        cardRecipeOfDay.isVisible = true
        textRecipeTitle.text = recipe.title
        textRecipeMeta.text = recipeMeta(recipe)

        val phaseLabel = recipe.phase.firstOrNull()
        if (recipeOfDay.personalized && phaseLabel != null) {
            textRecipePhase.isVisible = true
            textRecipePhase.text = getString(R.string.recipe_of_day_phase, phaseLabel.lowercase())
        } else {
            textRecipePhase.isVisible = false
        }

        buttonRecipeAction.setOnClickListener {
            Timber.i("Recette du jour ouverte : ${recipe.id}")
            val intent = Intent(requireContext(), RecetteDetailActivity::class.java)
                .putExtra(RecipeDetailViewModel.EXTRA_RECIPE_ID, recipe.id)
            startActivity(intent)
        }
    }

    /** Ligne « Catégorie · durée totale » de la carte recette. */
    private fun recipeMeta(recipe: Recipe): String {
        val category = categoryLabel(recipe.category)
        val minutes = (recipe.prepMinutes ?: 0) + (recipe.cookMinutes ?: 0)
        return if (minutes > 0) "$category · $minutes min" else category
    }

    private fun categoryLabel(category: RecipeCategory): String = when (category) {
        RecipeCategory.BREAKFAST -> "Petit-déjeuner"
        RecipeCategory.ENTREE -> "Entrée"
        RecipeCategory.PLAT -> "Plat"
        RecipeCategory.DESSERT -> "Dessert"
    }

    /**
     * Micronutriments adaptés à la phase du cycle : la carte reste masquée si la phase est
     * inconnue ou qu'aucune fiche n'est taguée (pas de repli, contrairement à la recette du jour
     * — cf. MicronutrientsForPhase).
     */
    private fun bindPhaseMicronutrients(nutrients: List<Micronutrient>, phase: CyclePhase?) {
        val phaseLabel = phase.toMicronutrientPhaseLabel()
        if (nutrients.isEmpty() || phaseLabel == null) {
            cardPhaseMicronutrients.isVisible = false
            return
        }
        cardPhaseMicronutrients.isVisible = true
        textPhaseMicronutrientsLabel.text = getString(R.string.micronutrients_phase_label, phaseLabel.lowercase())

        chipGroupPhaseMicronutrients.removeAllViews()
        val hasAccess = UserStore(requireContext()).hasContentAccess()
        nutrients.forEach { nutrient ->
            chipGroupPhaseMicronutrients.addView(nutrientChip(nutrient, hasAccess))
        }
    }

    private fun nutrientChip(nutrient: Micronutrient, hasAccess: Boolean): Chip {
        val chip = layoutInflater.inflate(R.layout.item_theme_chip, chipGroupPhaseMicronutrients, false) as Chip
        chip.id = View.generateViewId()
        chip.text = nutrient.name
        chip.isCheckable = false
        chip.setOnClickListener {
            if (nutrient.premium && !hasAccess) {
                startActivity(Intent(requireContext(), PremiumUpsellActivity::class.java))
                return@setOnClickListener
            }
            Timber.i("Micronutriment (Pour toi) ouvert : ${nutrient.id}")
            startActivity(
                Intent(requireContext(), MicronutrientDetailActivity::class.java)
                    .putExtra(MicronutrientDetailActivity.EXTRA_NUTRIENT_ID, nutrient.id)
            )
        }
        return chip
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
