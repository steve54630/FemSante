package com.audreyRetournayDiet.femSante.data.recipe

import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase
import java.time.LocalDate

/**
 * Recette proposée pour la journée.
 *
 * @param personalized `true` si la recette a été choisie **selon la phase du cycle**, `false`
 *   s'il s'agit d'un repli (phase indéterminée, profil sans cycle, ou aucune recette de la
 *   phase). Le repli sert à toujours proposer quelque chose sans jamais afficher d'information
 *   de phase potentiellement fausse (principe « zéro anxiété »).
 */
data class RecipeOfDay(val recipe: Recipe, val personalized: Boolean)

/**
 * Sélectionne « la recette du jour » de façon **pure et déterministe**.
 *
 * - Candidates = recettes dont la [Recipe.phase] correspond à la phase courante (ou taguées
 *   « Toutes »).
 * - Repli : si la phase est inconnue ([CyclePhase.INDETERMINEE] ou `null`) ou qu'aucune recette
 *   ne correspond, on tire dans l'ensemble du catalogue.
 * - Choix : index déterministe à partir du jour (`epochDay % taille`) → la même recette toute la
 *   journée, une rotation d'un jour à l'autre. L'ordre du catalogue étant stable, le résultat
 *   est reproductible (utile pour les tests).
 */
object DailyRecipeSelector {

    fun select(recipes: List<Recipe>, phase: CyclePhase?, date: LocalDate): RecipeOfDay? {
        if (recipes.isEmpty()) return null

        val label = phase.toRecipePhaseLabel()
        val candidates = if (label == null) {
            emptyList()
        } else {
            recipes.filter { it.matchesPhaseLabel(label) }
        }

        val personalized = candidates.isNotEmpty()
        val pool = if (personalized) candidates else recipes
        val index = date.toEpochDay().mod(pool.size) // mod(Int) renvoie toujours un index positif

        return RecipeOfDay(pool[index], personalized)
    }
}
