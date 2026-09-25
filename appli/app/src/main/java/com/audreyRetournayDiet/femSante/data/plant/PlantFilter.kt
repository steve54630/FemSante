package com.audreyRetournayDiet.femSante.data.plant

import java.text.Normalizer

/**
 * Filtrage pur des fiches du lexique de plantes, isolé de l'UI pour rester testable en JVM.
 *
 * Combinaison **ET** de deux critères indépendants :
 * - **catégories de symptôme** : la plante doit porter **toutes** les catégories cochées (même
 *   sémantique que [com.audreyRetournayDiet.femSante.data.recipe.RecipeFilter] pour les tags
 *   recettes) ;
 * - **recherche par nom** : sous-chaîne insensible aux accents/majuscules sur [Plant.name].
 */
object PlantFilter {

    fun filter(items: List<Plant>, categories: Set<PlantSymptomCategory>, query: String = ""): List<Plant> {
        val normalizedQuery = query.normalizeForSearch()
        return items.filter { plant ->
            categories.all { it in plant.symptomCategories } &&
                (normalizedQuery.isBlank() || plant.name.normalizeForSearch().contains(normalizedQuery))
        }
    }

    /** Catégories réellement présentes dans la liste, dans l'ordre de déclaration de l'enum. */
    fun categoriesFor(items: List<Plant>): List<PlantSymptomCategory> =
        items.flatMap { it.symptomCategories }.distinct().sortedBy { it.ordinal }

    private fun String.normalizeForSearch(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase()
}
