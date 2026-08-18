package com.audreyRetournayDiet.femSante.data.micronutrient

/**
 * Filtrage pur des fiches micronutriments (par groupe), isolé de l'UI pour rester testable en
 * JVM. Même patron que `MediaFilter` / `RecipeFilter`.
 */
object MicronutrientFilter {

    /** Fiches du [group] donné, ou toutes si [group] est nul. */
    fun filter(items: List<Micronutrient>, group: NutrientGroup?): List<Micronutrient> =
        items.filter { group == null || it.group == group }

    /** Groupes réellement présents dans la liste, dans l'ordre de déclaration de l'enum. */
    fun groupsFor(items: List<Micronutrient>): List<NutrientGroup> =
        items.map { it.group }.distinct().sortedBy { it.ordinal }
}
