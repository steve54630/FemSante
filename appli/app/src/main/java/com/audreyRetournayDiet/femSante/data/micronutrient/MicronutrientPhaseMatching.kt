package com.audreyRetournayDiet.femSante.data.micronutrient

import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase

/** Libellé de phase « fourre-tout » : un micronutriment ainsi tagué convient à toutes les phases. */
const val MICRONUTRIENT_PHASE_ALL = "Toutes"

/**
 * Traduit une phase de cycle en **libellé micronutriment** (`phase` dans `micronutrients.json`),
 * ou `null` si la phase n'est pas exploitable ([CyclePhase.INDETERMINEE] ou `null`).
 *
 * Même vocabulaire que [com.audreyRetournayDiet.femSante.data.recipe.toRecipePhaseLabel] (recette
 * du jour), dupliqué volontairement plutôt que partagé : les deux domaines (recettes,
 * micronutriments) restent indépendants, comme le reste de l'architecture par module de contenu.
 */
fun CyclePhase?.toMicronutrientPhaseLabel(): String? = when (this) {
    CyclePhase.MENSTRUELLE -> "Menstruelle"
    CyclePhase.FOLLICULAIRE -> "Folliculaire"
    CyclePhase.OVULATION -> "Ovulatoire"
    CyclePhase.LUTEALE -> "Lutéale"
    CyclePhase.INDETERMINEE, null -> null
}

/** `true` si le micronutriment correspond au libellé de phase donné (ou s'il convient à toutes). */
fun Micronutrient.matchesPhaseLabel(label: String): Boolean =
    phase.any { it.equals(label, ignoreCase = true) || it.equals(MICRONUTRIENT_PHASE_ALL, ignoreCase = true) }
