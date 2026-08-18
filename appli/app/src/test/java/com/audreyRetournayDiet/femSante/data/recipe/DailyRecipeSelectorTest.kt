package com.audreyRetournayDiet.femSante.data.recipe

import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Tests de la logique pure de sélection de « la recette du jour ».
 */
class DailyRecipeSelectorTest {

    private fun recipe(id: String, phase: String) = Recipe(
        id = id,
        category = RecipeCategory.PLAT,
        title = id,
        phase = listOf(phase)
    )

    private val folliculaire1 = recipe("f1", "Folliculaire")
    private val folliculaire2 = recipe("f2", "Folliculaire")
    private val luteale1 = recipe("l1", "Lutéale")
    private val catalog = listOf(folliculaire1, luteale1, folliculaire2)

    @Test
    fun `catalogue vide renvoie null`() {
        assertNull(DailyRecipeSelector.select(emptyList(), CyclePhase.FOLLICULAIRE, LocalDate.of(2026, 8, 18)))
    }

    @Test
    fun `choisit une recette de la phase et marque comme personnalisee`() {
        val result = DailyRecipeSelector.select(catalog, CyclePhase.FOLLICULAIRE, LocalDate.of(2026, 8, 18))!!
        assertTrue(result.personalized)
        assertEquals("Folliculaire", result.recipe.phase.single())
    }

    @Test
    fun `phase indeterminee bascule en repli non personnalise`() {
        val result = DailyRecipeSelector.select(catalog, CyclePhase.INDETERMINEE, LocalDate.of(2026, 8, 18))!!
        assertFalse(result.personalized)
    }

    @Test
    fun `phase nulle bascule en repli non personnalise`() {
        val result = DailyRecipeSelector.select(catalog, null, LocalDate.of(2026, 8, 18))!!
        assertFalse(result.personalized)
    }

    @Test
    fun `aucune recette de la phase bascule en repli sur tout le catalogue`() {
        val onlyLuteale = listOf(luteale1)
        val result = DailyRecipeSelector.select(onlyLuteale, CyclePhase.MENSTRUELLE, LocalDate.of(2026, 8, 18))!!
        assertFalse(result.personalized)
        assertEquals("l1", result.recipe.id)
    }

    @Test
    fun `resultat stable pour une meme date`() {
        val date = LocalDate.of(2026, 8, 18)
        val a = DailyRecipeSelector.select(catalog, CyclePhase.FOLLICULAIRE, date)!!
        val b = DailyRecipeSelector.select(catalog, CyclePhase.FOLLICULAIRE, date)!!
        assertEquals(a.recipe.id, b.recipe.id)
    }

    @Test
    fun `rotation entre les candidates au fil des jours`() {
        // 2 candidates folliculaires -> deux jours consécutifs doivent alterner.
        val day0 = LocalDate.of(2026, 8, 18)
        val day1 = day0.plusDays(1)
        val r0 = DailyRecipeSelector.select(catalog, CyclePhase.FOLLICULAIRE, day0)!!
        val r1 = DailyRecipeSelector.select(catalog, CyclePhase.FOLLICULAIRE, day1)!!
        assertTrue(r0.recipe.id in setOf("f1", "f2"))
        assertTrue(r1.recipe.id in setOf("f1", "f2"))
        assertTrue("deux jours consécutifs devraient donner des recettes différentes", r0.recipe.id != r1.recipe.id)
    }
}
