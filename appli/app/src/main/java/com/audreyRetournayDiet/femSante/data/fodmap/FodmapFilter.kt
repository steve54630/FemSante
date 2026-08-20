package com.audreyRetournayDiet.femSante.data.fodmap

import java.text.Normalizer

/** Filtrage pur du guide FODMAP : recherche par nom (insensible accents/casse) + statut. */
object FodmapFilter {

    fun filter(foods: List<FodmapFood>, query: String, status: FodmapStatus?): List<FodmapFood> {
        val normalizedQuery = normalize(query)
        return foods.filter { food ->
            (status == null || food.status == status) &&
                (normalizedQuery.isBlank() || normalize(food.name).contains(normalizedQuery))
        }
    }

    private fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .lowercase()

    private val DIACRITICS = Regex("\\p{Mn}+")
}
