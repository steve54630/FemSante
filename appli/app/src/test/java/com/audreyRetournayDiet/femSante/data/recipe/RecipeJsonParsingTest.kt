package com.audreyRetournayDiet.femSante.data.recipe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste la **logique de parsing** des recettes sur un mini-fixture dédié
 * (`test/resources/recipes_sample.json`).
 *
 * On ne parse **pas** le vrai `assets/recipes.json` : c'est de la donnée (susceptible de passer
 * côté API), la coupler aux tests les rendrait fragiles. Le fixture ne valide que le parseur :
 * champs obligatoires, résolution de l'enum de catégorie, unicité des identifiants.
 */
class RecipeJsonParsingTest {

    private val recipes: List<Recipe> by lazy {
        val json = javaClass.getResourceAsStream("/recipes_sample.json")!!
            .bufferedReader().use { it.readText() }
        RecipeJsonParser.parse(json)
    }

    @Test
    fun `parse toutes les recettes du fixture`() {
        assertEquals(2, recipes.size)
    }

    @Test
    fun `chaque recette a un id, un titre, des ingredients et des etapes`() {
        recipes.forEach { recipe ->
            assertTrue("id vide", recipe.id.isNotBlank())
            assertTrue("titre vide pour '${recipe.id}'", recipe.title.isNotBlank())
            assertTrue("aucun ingrédient pour '${recipe.id}'", recipe.ingredients.isNotEmpty())
            assertTrue("aucune étape pour '${recipe.id}'", recipe.steps.isNotEmpty())
            recipe.ingredients.forEach { assertTrue("nom d'ingrédient vide dans '${recipe.id}'", it.name.isNotBlank()) }
            recipe.steps.forEach { assertTrue("étape vide dans '${recipe.id}'", it.isNotBlank()) }
        }
    }

    @Test
    fun `les categories sont resolues par nom`() {
        // Gson mettrait la catégorie à null si le libellé ne correspondait à aucune valeur d'enum.
        assertTrue(recipes.all { it.category != null })
        assertEquals(RecipeCategory.BREAKFAST, recipes.first { it.id == "sample_breakfast" }.category)
        assertEquals(RecipeCategory.DESSERT, recipes.first { it.id == "sample_dessert" }.category)
    }

    @Test
    fun `les identifiants sont uniques`() {
        val ids = recipes.map { it.id }
        assertEquals("des identifiants sont dupliqués", ids.size, ids.toSet().size)
    }
}
