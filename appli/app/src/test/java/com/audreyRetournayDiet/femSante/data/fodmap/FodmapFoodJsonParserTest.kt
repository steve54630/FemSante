package com.audreyRetournayDiet.femSante.data.fodmap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste [FodmapFoodJsonParser] sur un mini-fixture dédié (`test/resources/fodmap_sample.json`) —
 * pas les vrais assets de prod. Valide le mapping des 3 statuts et le rejet des entrées de
 * catégorie/statut inconnus.
 */
class FodmapFoodJsonParserTest {

    private val foods: List<FodmapFood> by lazy {
        val json = javaClass.getResourceAsStream("/fodmap_sample.json")!!
            .bufferedReader().use { it.readText() }
        FodmapFoodJsonParser.parse(json)
    }

    @Test
    fun `les entrees de categorie ou statut inconnus sont ignorees`() {
        assertEquals(3, foods.size)
    }

    @Test
    fun `les 3 statuts sont correctement resolus`() {
        assertEquals(FodmapStatus.PAUVRE, foods.first { it.id == "sample_pauvre" }.status)
        assertEquals(FodmapStatus.MODERE, foods.first { it.id == "sample_modere" }.status)
        assertEquals(FodmapStatus.RICHE, foods.first { it.id == "sample_riche" }.status)
    }

    @Test
    fun `portion et notes sont conservees`() {
        val aubergine = foods.first { it.id == "sample_modere" }
        assertEquals("75g", aubergine.portion)
        assertEquals("Blanchir réduit les FODMAPs", aubergine.notes)
        assertTrue(aubergine.category == FodmapCategory.LEGUMES)
    }
}
