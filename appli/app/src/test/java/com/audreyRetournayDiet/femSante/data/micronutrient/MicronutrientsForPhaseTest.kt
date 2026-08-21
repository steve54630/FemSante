package com.audreyRetournayDiet.femSante.data.micronutrient

import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la logique pure de sélection des micronutriments adaptés à la phase, pour la carte
 * "Pour toi". Contrairement à [com.audreyRetournayDiet.femSante.data.recipe.DailyRecipeSelector]
 * (recette du jour), il n'y a **aucun repli** : phase inconnue -> liste vide.
 */
class MicronutrientsForPhaseTest {

    private fun nutrient(id: String, phase: List<String>) = Micronutrient(
        id = id,
        name = id,
        group = NutrientGroup.MINERAL,
        unit = "mg",
        intake = NutrientIntake(women = "0", men = "0", pregnant = "0", breastfeeding = "0"),
        phase = phase
    )

    private val folliculaireSeul = nutrient("f1", listOf("Folliculaire"))
    private val luteale = nutrient("l1", listOf("Lutéale"))
    private val toutesPhases = nutrient("t1", listOf("Toutes"))
    private val nonTague = nutrient("n1", emptyList())
    private val catalog = listOf(folliculaireSeul, luteale, toutesPhases, nonTague)

    @Test
    fun `phase nulle renvoie une liste vide (pas de repli)`() {
        assertTrue(MicronutrientsForPhase.select(catalog, null).isEmpty())
    }

    @Test
    fun `phase indeterminee renvoie une liste vide`() {
        assertTrue(MicronutrientsForPhase.select(catalog, CyclePhase.INDETERMINEE).isEmpty())
    }

    @Test
    fun `phase connue inclut les fiches taguees pour cette phase et celles taguees Toutes`() {
        val result = MicronutrientsForPhase.select(catalog, CyclePhase.FOLLICULAIRE)
        assertEquals(setOf("f1", "t1"), result.map { it.id }.toSet())
    }

    @Test
    fun `une fiche non taguee n'apparait jamais`() {
        val result = MicronutrientsForPhase.select(catalog, CyclePhase.FOLLICULAIRE)
        assertTrue(result.none { it.id == "n1" })
    }

    @Test
    fun `aucune fiche taguee pour la phase renvoie une liste vide`() {
        val result = MicronutrientsForPhase.select(listOf(folliculaireSeul), CyclePhase.LUTEALE)
        assertTrue(result.isEmpty())
    }
}
