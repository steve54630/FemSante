package com.audreyRetournayDiet.femSante.data.recipe

/**
 * Liste ordonnée des tags diététiques utilisés pour filtrer les recettes.
 *
 * Source unique de vérité pour la feuille de filtres. Doit rester alignée sur les libellés
 * réellement présents dans `recipes.json` (renseignés par la diététicienne).
 */
object RecipeTags {
    val ALL: List<String> = listOf(
        "Sans gluten",
        "Express",
        "Sans cuisson",
        "Végétarien",
        "Végétalien",
        "Sans produits laitiers",
        "Sans fruits à coque",
        "Riche en protéines",
        "Index Glycémique bas",
        "Riche en fer",
        "Confort digestif"
    )
}
