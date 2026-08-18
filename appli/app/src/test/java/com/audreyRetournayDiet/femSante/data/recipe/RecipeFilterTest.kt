package com.audreyRetournayDiet.femSante.data.recipe

import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la logique pure de filtrage des recettes (écran de navigation).
 */
class RecipeFilterTest {

    private fun recipe(
        id: String,
        category: RecipeCategory,
        phase: String,
        tags: List<String>
    ) = Recipe(id = id, category = category, title = id, phase = listOf(phase), tags = tags)

    private val veloute = recipe("veloute", RecipeCategory.ENTREE, "Menstruelle", listOf("Sans gluten", "Végétalien"))
    private val houmous = recipe("houmous", RecipeCategory.ENTREE, "Folliculaire", listOf("Sans gluten", "Express"))
    private val fondant = recipe("fondant", RecipeCategory.DESSERT, "Menstruelle", listOf("Sans gluten"))
    private val catalog = listOf(veloute, houmous, fondant)

    @Test
    fun `sans filtre renvoie tout`() {
        val result = RecipeFilter.filter(catalog, category = null, tags = emptySet(), currentPhase = null, phaseOnly = false)
        assertEquals(catalog, result)
    }

    @Test
    fun `filtre par categorie`() {
        val result = RecipeFilter.filter(catalog, category = RecipeCategory.ENTREE, tags = emptySet(), currentPhase = null, phaseOnly = false)
        assertEquals(listOf(veloute, houmous), result)
    }

    @Test
    fun `un tag ne garde que les recettes qui le portent`() {
        val result = RecipeFilter.filter(catalog, category = null, tags = setOf("Express"), currentPhase = null, phaseOnly = false)
        assertEquals(listOf(houmous), result)
    }

    @Test
    fun `plusieurs tags fonctionnent en ET`() {
        // "Sans gluten" + "Végétalien" : seul le velouté a les deux.
        val result = RecipeFilter.filter(catalog, category = null, tags = setOf("Sans gluten", "Végétalien"), currentPhase = null, phaseOnly = false)
        assertEquals(listOf(veloute), result)
    }

    @Test
    fun `ma phase ne garde que les recettes de la phase courante`() {
        val result = RecipeFilter.filter(catalog, category = null, tags = emptySet(), currentPhase = CyclePhase.MENSTRUELLE, phaseOnly = true)
        assertEquals(listOf(veloute, fondant), result)
    }

    @Test
    fun `ma phase avec phase inconnue est ignoree (aucune contrainte)`() {
        val result = RecipeFilter.filter(catalog, category = null, tags = emptySet(), currentPhase = CyclePhase.INDETERMINEE, phaseOnly = true)
        assertEquals(catalog, result)
    }

    @Test
    fun `combinaison categorie phase et tag`() {
        // Entrée + phase folliculaire + Express -> houmous seulement.
        val result = RecipeFilter.filter(
            catalog,
            category = RecipeCategory.ENTREE,
            tags = setOf("Express"),
            currentPhase = CyclePhase.FOLLICULAIRE,
            phaseOnly = true
        )
        assertEquals(listOf(houmous), result)
    }

    @Test
    fun `aucune correspondance renvoie une liste vide`() {
        val result = RecipeFilter.filter(catalog, category = RecipeCategory.DESSERT, tags = setOf("Express"), currentPhase = null, phaseOnly = false)
        assertTrue(result.isEmpty())
    }
}
