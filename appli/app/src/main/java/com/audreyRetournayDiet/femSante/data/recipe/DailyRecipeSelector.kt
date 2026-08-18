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

    /** Libellé de phase « fourre-tout » : une recette ainsi taguée convient à toutes les phases. */
    private const val ALL_PHASES = "Toutes"

    fun select(recipes: List<Recipe>, phase: CyclePhase?, date: LocalDate): RecipeOfDay? {
        if (recipes.isEmpty()) return null

        val label = phaseLabel(phase)
        val candidates = if (label == null) {
            emptyList()
        } else {
            recipes.filter { recipe ->
                recipe.phase.any { it.equals(label, ignoreCase = true) || it.equals(ALL_PHASES, ignoreCase = true) }
            }
        }

        val personalized = candidates.isNotEmpty()
        val pool = if (personalized) candidates else recipes
        val index = date.toEpochDay().mod(pool.size) // mod(Int) renvoie toujours un index positif

        return RecipeOfDay(pool[index], personalized)
    }

    /** Traduit la phase du cycle en libellé de recette, ou `null` si la phase n'est pas exploitable. */
    private fun phaseLabel(phase: CyclePhase?): String? = when (phase) {
        CyclePhase.MENSTRUELLE -> "Menstruelle"
        CyclePhase.FOLLICULAIRE -> "Folliculaire"
        CyclePhase.OVULATION -> "Ovulatoire"
        CyclePhase.LUTEALE -> "Lutéale"
        CyclePhase.INDETERMINEE, null -> null
    }
}
