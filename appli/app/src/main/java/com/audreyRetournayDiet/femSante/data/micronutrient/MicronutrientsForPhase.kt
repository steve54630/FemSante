package com.audreyRetournayDiet.femSante.data.micronutrient

import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase

/**
 * Sélectionne les micronutriments adaptés à la phase du cycle courante, pour la carte "Pour toi".
 *
 * Contrairement à la recette du jour
 * ([com.audreyRetournayDiet.femSante.data.recipe.DailyRecipeSelector]) : **aucun repli**. Si la
 * phase est inconnue ([CyclePhase.INDETERMINEE] ou `null`) ou qu'aucune fiche n'est taguée pour
 * cette phase, la liste est vide — la carte reste alors masquée plutôt que d'afficher une
 * sélection non pertinente (décision produit : pas de filtre par défaut ici).
 */
object MicronutrientsForPhase {

    fun select(nutrients: List<Micronutrient>, phase: CyclePhase?): List<Micronutrient> {
        val label = phase.toMicronutrientPhaseLabel() ?: return emptyList()
        return nutrients.filter { it.matchesPhaseLabel(label) }
    }
}
