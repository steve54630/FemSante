package com.audreyRetournayDiet.femSante.data.micronutrient

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste le **filtrage pur** ([MicronutrientFilter]) sur le mini-fixture dédié (un contenu par
 * groupe) — pas les vrais assets de prod (cf. [MicronutrientJsonParsingTest]).
 */
class MicronutrientFilterTest {

    private val nutrients: List<Micronutrient> by lazy {
        val json = javaClass.getResourceAsStream("/micronutrients_sample.json")!!
            .bufferedReader().use { it.readText() }
        MicronutrientJsonParser.parseNutrients(json)
    }

    @Test
    fun `les groupes presents sont dans l'ordre de l'enum`() {
        assertEquals(
            listOf(
                NutrientGroup.LIPOSOLUBLE,
                NutrientGroup.HYDROSOLUBLE,
                NutrientGroup.MINERAL,
                NutrientGroup.OLIGO
            ),
            MicronutrientFilter.groupsFor(nutrients)
        )
    }

    @Test
    fun `filtre par groupe`() {
        assertEquals(1, MicronutrientFilter.filter(nutrients, NutrientGroup.LIPOSOLUBLE).size)
        assertEquals(1, MicronutrientFilter.filter(nutrients, NutrientGroup.HYDROSOLUBLE).size)
        assertEquals(1, MicronutrientFilter.filter(nutrients, NutrientGroup.MINERAL).size)
        assertEquals(1, MicronutrientFilter.filter(nutrients, NutrientGroup.OLIGO).size)
    }

    @Test
    fun `sans groupe on obtient toutes les fiches`() {
        val all = MicronutrientFilter.filter(nutrients, group = null)
        assertEquals(4, all.size)
        assertTrue(all.all { it.group != null })
    }
}
