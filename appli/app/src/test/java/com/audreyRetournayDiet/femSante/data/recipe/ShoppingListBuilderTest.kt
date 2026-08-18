package com.audreyRetournayDiet.femSante.data.recipe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de l'agrégation pure de la liste de courses.
 */
class ShoppingListBuilderTest {

    private fun ing(name: String, quantity: String?, unit: String?) =
        RecipeIngredient(name = name, quantity = quantity, unit = unit)

    private fun recipe(id: String, ingredients: List<RecipeIngredient>) =
        Recipe(id = id, category = RecipeCategory.PLAT, title = id, ingredients = ingredients)

    /** Raccourci : une sélection à réaliser [count] fois (1 par défaut). */
    private fun sel(recipe: Recipe, count: Int = 1) = RecipeSelection(recipe, count)

    @Test
    fun `additionne les quantites de meme unite`() {
        val a = recipe("a", listOf(ing("Farine de sarrasin", "45", "g")))
        val b = recipe("b", listOf(ing("Farine de sarrasin", "45", "g")))
        val item = ShoppingListBuilder.build(listOf(sel(a), sel(b))).single().items.single()
        assertEquals("Farine de sarrasin", item.name)
        assertEquals(listOf("90 g"), item.quantities)
    }

    @Test
    fun `realiser une recette plusieurs fois multiplie ses quantites`() {
        val veloute = recipe("veloute", listOf(ing("Épinards", "250", "g")))
        val item = ShoppingListBuilder.build(listOf(sel(veloute, count = 2))).single().items.single()
        assertEquals(listOf("500 g"), item.quantities)
    }

    @Test
    fun `le multiplicateur se combine entre recettes`() {
        val a = recipe("a", listOf(ing("Farine de sarrasin", "100", "g")))
        val b = recipe("b", listOf(ing("Farine de sarrasin", "50", "g")))
        // a réalisé 2 fois (200 g) + b une fois (50 g) = 250 g
        val item = ShoppingListBuilder.build(listOf(sel(a, 2), sel(b, 1))).single().items.single()
        assertEquals(listOf("250 g"), item.quantities)
    }

    @Test
    fun `liste les unites differentes cote a cote`() {
        val a = recipe("a", listOf(ing("Épinards", "250", "g")))
        val b = recipe("b", listOf(ing("Épinards", "3", "poignée")))
        val item = ShoppingListBuilder.build(listOf(sel(a), sel(b))).single().items.single()
        assertEquals(listOf("250 g", "3 poignée"), item.quantities)
        assertEquals(Rayon.FRUITS_LEGUMES, item.rayon)
    }

    @Test
    fun `un ingredient sans quantite est listé sans quantite et non multiplie`() {
        val a = recipe("a", listOf(ing("Sel, poivre", null, null)))
        val item = ShoppingListBuilder.build(listOf(sel(a, count = 3))).single().items.single()
        assertTrue(item.quantities.isEmpty())
        assertEquals(Rayon.EPICES, item.rayon)
    }

    @Test
    fun `l'eau est exclue`() {
        val a = recipe("a", listOf(ing("Eau", "30", "cl"), ing("Carottes", "3", "pièce")))
        val allNames = ShoppingListBuilder.build(listOf(sel(a))).flatMap { it.items }.map { it.name }
        assertTrue("Eau" !in allNames)
        assertTrue("Carottes" in allNames)
    }

    @Test
    fun `les sections sont ordonnees selon le parcours magasin`() {
        val a = recipe("a", listOf(
            ing("Sel", null, null),               // Épices
            ing("Carottes", "2", "pièce"),        // Fruits et légumes
            ing("Farine de sarrasin", "100", "g") // Épicerie sèche
        ))
        val rayons = ShoppingListBuilder.build(listOf(sel(a))).map { it.rayon }
        assertEquals(listOf(Rayon.FRUITS_LEGUMES, Rayon.EPICERIE_SECHE, Rayon.EPICES), rayons)
    }

    @Test
    fun `les demi-quantites restent lisibles`() {
        val a = recipe("a", listOf(ing("Avocat", "0.25", "pièce")))
        val b = recipe("b", listOf(ing("Avocat", "0.25", "pièce")))
        val item = ShoppingListBuilder.build(listOf(sel(a), sel(b))).single().items.single()
        assertEquals(listOf("0.5 pièce"), item.quantities)
    }

    @Test
    fun `liste vide sans recette selectionnee`() {
        assertTrue(ShoppingListBuilder.build(emptyList()).isEmpty())
    }
}
