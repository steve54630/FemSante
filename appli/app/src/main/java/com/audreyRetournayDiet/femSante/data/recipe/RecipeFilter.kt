package com.audreyRetournayDiet.femSante.data.recipe

import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase

/**
 * Filtre pur du catalogue de recettes pour l'écran de navigation.
 *
 * Combinaison **ET** de trois critères indépendants :
 * - **catégorie** : `null` = toutes ;
 * - **phase** : si [phaseOnly] est actif et que la phase courante est exploitable, on ne garde
 *   que les recettes de cette phase (le filtre est ignoré si la phase est inconnue, pour ne
 *   jamais renvoyer un écran vide inexplicable — le bouton « Ma phase » est de toute façon
 *   masqué dans ce cas côté UI) ;
 * - **tags** : la recette doit porter **tous** les tags cochés.
 */
object RecipeFilter {

    fun filter(
        recipes: List<Recipe>,
        category: RecipeCategory?,
        tags: Set<String>,
        currentPhase: CyclePhase?,
        phaseOnly: Boolean
    ): List<Recipe> {
        val phaseLabel = if (phaseOnly) currentPhase.toRecipePhaseLabel() else null

        return recipes.filter { recipe ->
            (category == null || recipe.category == category) &&
                (phaseLabel == null || recipe.matchesPhaseLabel(phaseLabel)) &&
                tags.all { tag -> recipe.tags.any { it.equals(tag, ignoreCase = true) } }
        }
    }
}
