package com.audreyRetournayDiet.femSante.data.recipe

/** Une recette retenue pour la liste de courses, avec le nombre de fois qu'on veut la réaliser. */
data class RecipeSelection(val recipe: Recipe, val count: Int)

/** Un ingrédient agrégé de la liste de courses. */
data class ShoppingItem(
    val name: String,
    /**
     * Quantités agrégées, prêtes à afficher (ex. `["350 g", "3 poignée"]`) : sommées quand l'unité
     * est identique, listées sinon. Vide si l'ingrédient n'a aucune quantité chiffrée (ex. « qs »).
     */
    val quantities: List<String>,
    val rayon: Rayon
)

/** Une section de la liste de courses (un rayon + ses ingrédients). */
data class ShoppingSection(
    val rayon: Rayon,
    val items: List<ShoppingItem>
)

/**
 * Construit la liste de courses à partir des recettes sélectionnées, **chacune avec sa quantité**
 * ([RecipeSelection.count]) : réaliser 2 fois une recette double ses ingrédients chiffrés.
 *
 * - Regroupe les ingrédients par **aliment** (dédoublonnage), puis par **rayon** (ordre du
 *   parcours magasin défini par [Rayon]).
 * - Additionne les quantités **de même unité** (250 g + 100 g = 350 g), et **liste** les unités
 *   différentes côte à côte (l'addition d'unités hétérogènes ou de « quantité suffisante » n'aurait
 *   pas de sens). Le multiplicateur ne s'applique qu'aux quantités chiffrées.
 * - Exclut l'eau (jamais achetée).
 *
 * Logique pure et déterministe → testable sans Android.
 */
object ShoppingListBuilder {

    private val EXCLUDED = setOf("Eau", "Eau tiède")

    fun build(selections: List<RecipeSelection>): List<ShoppingSection> {
        val byName = LinkedHashMap<String, MutableList<Occurrence>>()
        selections.forEach { (recipe, count) ->
            recipe.ingredients.forEach { ingredient ->
                if (ingredient.name !in EXCLUDED) {
                    byName.getOrPut(ingredient.name) { mutableListOf() }.add(Occurrence(ingredient, count))
                }
            }
        }

        return byName
            .map { (name, occurrences) ->
                ShoppingItem(
                    name = name,
                    quantities = summarizeQuantities(occurrences),
                    rayon = RayonClassifier.rayonFor(name)
                )
            }
            .groupBy { it.rayon }
            .toList()
            .sortedBy { (rayon, _) -> rayon.ordinal }
            .map { (rayon, items) -> ShoppingSection(rayon, items.sortedBy { it.name.lowercase() }) }
    }

    /** Somme par unité (× le nombre de réalisations), ou liste les unités distinctes ; ignore les « qs ». */
    private fun summarizeQuantities(occurrences: List<Occurrence>): List<String> {
        val byUnit = LinkedHashMap<String?, MutableList<Double>>()
        occurrences.forEach { (ingredient, count) ->
            val value = parseQuantity(ingredient.quantity) ?: return@forEach
            byUnit.getOrPut(ingredient.unit) { mutableListOf() }.add(value * count)
        }
        return byUnit.map { (unit, values) ->
            val sum = formatNumber(values.sum())
            if (unit.isNullOrBlank()) sum else "$sum $unit"
        }
    }

    private fun parseQuantity(quantity: String?): Double? =
        quantity?.trim()?.replace(',', '.')?.toDoubleOrNull()

    private fun formatNumber(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

    private data class Occurrence(val ingredient: RecipeIngredient, val count: Int)
}
