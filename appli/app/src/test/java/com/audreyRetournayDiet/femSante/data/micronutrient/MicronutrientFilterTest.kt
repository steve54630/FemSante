package com.audreyRetournayDiet.femSante.data.micronutrient

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Tests du filtrage pur, sur le catalogue micronutriments **réel** (asset livré).
 */
class MicronutrientFilterTest {

    private fun loadAsset(name: String): String {
        val candidates = listOf("src/main/assets/$name", "app/src/main/assets/$name")
        return candidates.map(::File).first { it.exists() }.readText()
    }

    private val nutrients: List<Micronutrient> by lazy {
        MicronutrientJsonParser.parseNutrients(loadAsset("micronutrients.json"))
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
        assertEquals(4, MicronutrientFilter.filter(nutrients, NutrientGroup.LIPOSOLUBLE).size)
        assertEquals(9, MicronutrientFilter.filter(nutrients, NutrientGroup.HYDROSOLUBLE).size)
        assertEquals(5, MicronutrientFilter.filter(nutrients, NutrientGroup.MINERAL).size)
        assertEquals(4, MicronutrientFilter.filter(nutrients, NutrientGroup.OLIGO).size)
    }

    @Test
    fun `sans groupe on obtient toutes les fiches`() {
        val all = MicronutrientFilter.filter(nutrients, group = null)
        assertEquals(22, all.size)
        assertTrue(all.all { it.group != null })
    }
}
