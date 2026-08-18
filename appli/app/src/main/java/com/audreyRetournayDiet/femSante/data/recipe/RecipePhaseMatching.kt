package com.audreyRetournayDiet.femSante.data.recipe

import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase

/** Libellé de phase « fourre-tout » : une recette ainsi taguée convient à toutes les phases. */
const val RECIPE_PHASE_ALL = "Toutes"

/**
 * Traduit une phase de cycle en **libellé de recette** (`phase` dans `recipes.json`), ou `null`
 * si la phase n'est pas exploitable ([CyclePhase.INDETERMINEE] ou `null`).
 *
 * Point de vérité unique partagé par [DailyRecipeSelector] (recette du jour) et [RecipeFilter]
 * (filtre « Ma phase »).
 */
fun CyclePhase?.toRecipePhaseLabel(): String? = when (this) {
    CyclePhase.MENSTRUELLE -> "Menstruelle"
    CyclePhase.FOLLICULAIRE -> "Folliculaire"
    CyclePhase.OVULATION -> "Ovulatoire"
    CyclePhase.LUTEALE -> "Lutéale"
    CyclePhase.INDETERMINEE, null -> null
}

/** `true` si la recette correspond au libellé de phase donné (ou si elle convient à toutes). */
fun Recipe.matchesPhaseLabel(label: String): Boolean =
    phase.any { it.equals(label, ignoreCase = true) || it.equals(RECIPE_PHASE_ALL, ignoreCase = true) }
