package com.audreyRetournayDiet.femSante.data.recipe

/**
 * Modèle d'une recette affichée nativement (fiche recette), par opposition au PDF figé.
 *
 * Les données proviennent du fichier statique `assets/recipes.json`, généré à partir des
 * fiches PDF de la diététicienne. L'[id] correspond au **nom de fichier PDF sans extension**
 * (ex. `veloute_epinards_amandes`) : c'est la clé de jointure avec le flux de navigation
 * existant (spinner + image + PDF de repli).
 *
 * [phase] et [tags] sont vides pour l'instant : ils seront renseignés par le tagging métier
 * de la diététicienne (filtrage par phase du cycle, recommandations).
 */
data class Recipe(
    val id: String,
    val category: RecipeCategory,
    val title: String,
    val servings: String? = null,
    val prepMinutes: Int? = null,
    val cookMinutes: Int? = null,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val nutritionTip: String? = null,
    val phase: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

/**
 * Un ingrédient d'une recette.
 *
 * [quantity] et [unit] sont volontairement optionnels : certaines fiches indiquent des
 * quantités libres (« un filet », « une poignée », « selon les goûts »). [note] conserve les
 * précisions du PDF (« ou surgelés », « idéalement origine France »…) et [section] regroupe
 * les ingrédients par sous-partie de la recette (« Pour la pâte », « Pour la garniture »,
 * « Topping »…).
 */
data class RecipeIngredient(
    val name: String,
    val quantity: String? = null,
    val unit: String? = null,
    val note: String? = null,
    val section: String? = null
)

/** Catégorie de recette, alignée sur les dossiers d'assets (breakfast/entries/main_courses/desserts). */
enum class RecipeCategory {
    BREAKFAST,
    ENTREE,
    PLAT,
    DESSERT
}
