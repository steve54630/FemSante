package com.audreyRetournayDiet.femSante.data.plant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste le **filtrage pur** ([PlantFilter]) sur le mini-fixture dédié — pas les vrais assets de
 * prod (cf. [PlantJsonParserTest]). Le fixture contient 5 entrées, 3 valides après parsing :
 * `plante_complete` (« Plante complète », DIGESTION_TRANSIT, REGLES_CYCLE),
 * `plante_minimale` (« Plante minimale », aucune catégorie),
 * `plante_stress` (« Plante stress », STRESS_SOMMEIL).
 */
class PlantFilterTest {

    private val plants: List<Plant> by lazy {
        val json = javaClass.getResourceAsStream("/plant_lexicon_sample.json")!!
            .bufferedReader().use { it.readText() }
        PlantJsonParser.parse(json)
    }

    @Test
    fun `les categories presentes sont dans l'ordre de l'enum`() {
        assertEquals(
            listOf(PlantSymptomCategory.DIGESTION_TRANSIT, PlantSymptomCategory.REGLES_CYCLE, PlantSymptomCategory.STRESS_SOMMEIL),
            PlantFilter.categoriesFor(plants)
        )
    }

    @Test
    fun `filtre par categorie unique`() {
        assertEquals(1, PlantFilter.filter(plants, setOf(PlantSymptomCategory.STRESS_SOMMEIL)).size)
        assertEquals(1, PlantFilter.filter(plants, setOf(PlantSymptomCategory.DIGESTION_TRANSIT)).size)
        assertTrue(PlantFilter.filter(plants, setOf(PlantSymptomCategory.FOIE_DETOX)).isEmpty())
    }

    @Test
    fun `plusieurs categories cochees = ET (doit porter toutes les categories)`() {
        // plante_complete porte DIGESTION_TRANSIT ET REGLES_CYCLE -> matche.
        val both = setOf(PlantSymptomCategory.DIGESTION_TRANSIT, PlantSymptomCategory.REGLES_CYCLE)
        assertEquals(1, PlantFilter.filter(plants, both).size)

        // plante_stress ne porte que STRESS_SOMMEIL, pas DIGESTION_TRANSIT -> exclue.
        val impossible = setOf(PlantSymptomCategory.DIGESTION_TRANSIT, PlantSymptomCategory.STRESS_SOMMEIL)
        assertTrue(PlantFilter.filter(plants, impossible).isEmpty())
    }

    @Test
    fun `recherche par nom insensible aux accents et a la casse`() {
        assertEquals(1, PlantFilter.filter(plants, emptySet(), "complète").size)
        assertEquals(1, PlantFilter.filter(plants, emptySet(), "complete").size) // sans accent
        assertEquals(1, PlantFilter.filter(plants, emptySet(), "COMPLETE").size) // casse
        assertEquals(1, PlantFilter.filter(plants, emptySet(), "plante compl").size) // sous-chaine
        assertTrue(PlantFilter.filter(plants, emptySet(), "inexistante").isEmpty())
    }

    @Test
    fun `sans filtre on obtient toutes les fiches`() {
        assertEquals(3, PlantFilter.filter(plants, emptySet(), "").size)
    }
}
