package com.audreyRetournayDiet.femSante.data.recipe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Valide le fichier réellement livré `assets/recipes.json` (pas un échantillon).
 *
 * Le test lit le fichier directement sur le disque (le répertoire de travail des tests
 * unitaires Android est le dossier du module) puis le parse via [RecipeJsonParser], sans
 * dépendre d'un `Context`/`AssetManager`. Objectif : détecter au plus tôt une faute de frappe,
 * une virgule manquante ou une catégorie invalide, avant même de brancher l'UI.
 */
class RecipeJsonParsingTest {

    private fun loadRecipesJson(): String {
        val candidates = listOf(
            "src/main/assets/recipes.json",
            "app/src/main/assets/recipes.json"
        )
        val file = candidates.map(::File).firstOrNull { it.exists() }
            ?: error("recipes.json introuvable (répertoire courant : ${File("").absolutePath})")
        return file.readText()
    }

    private val recipes: List<Recipe> by lazy { RecipeJsonParser.parse(loadRecipesJson()) }

    @Test
    fun `le fichier contient les 20 recettes`() {
        assertEquals(20, recipes.size)
    }

    @Test
    fun `chaque recette a un id, un titre, des ingredients et des etapes`() {
        recipes.forEach { recipe ->
            assertTrue("id vide", recipe.id.isNotBlank())
            assertTrue("titre vide pour '${recipe.id}'", recipe.title.isNotBlank())
            assertTrue("aucun ingrédient pour '${recipe.id}'", recipe.ingredients.isNotEmpty())
            assertTrue("aucune étape pour '${recipe.id}'", recipe.steps.isNotEmpty())
            recipe.ingredients.forEach { ingredient ->
                assertTrue("nom d'ingrédient vide dans '${recipe.id}'", ingredient.name.isNotBlank())
            }
            recipe.steps.forEach { step ->
                assertTrue("étape vide dans '${recipe.id}'", step.isNotBlank())
            }
        }
    }

    @Test
    fun `les identifiants sont uniques`() {
        val ids = recipes.map { it.id }
        assertEquals("des identifiants sont dupliqués", ids.size, ids.toSet().size)
    }

    @Test
    fun `les categories sont valides et correctement reparties`() {
        // Gson mettrait la catégorie à null si le libellé ne correspondait à aucune valeur de
        // l'enum ; on vérifie donc à la fois qu'aucune n'est nulle et la répartition attendue.
        assertTrue(recipes.all { it.category != null })
        val countByCategory = recipes.groupingBy { it.category }.eachCount()
        assertEquals(2, countByCategory[RecipeCategory.BREAKFAST])
        assertEquals(5, countByCategory[RecipeCategory.ENTREE])
        assertEquals(6, countByCategory[RecipeCategory.PLAT])
        assertEquals(7, countByCategory[RecipeCategory.DESSERT])
    }
}
