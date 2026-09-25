package com.audreyRetournayDiet.femSante.data.plant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste [PlantJsonParser] sur un mini-fixture dédié (`test/resources/plant_lexicon_sample.json`) —
 * pas les vrais assets de prod. Valide le mapping des champs, les listes optionnelles et
 * l'exclusion des fiches sans nom ou sans identifiant.
 */
class PlantJsonParserTest {

    private val plants: List<Plant> by lazy {
        val json = javaClass.getResourceAsStream("/plant_lexicon_sample.json")!!
            .bufferedReader().use { it.readText() }
        PlantJsonParser.parse(json)
    }

    @Test
    fun `les fiches sans nom ou sans identifiant sont ignorees`() {
        // Le sample contient 5 entrées : 3 valides, 1 sans nom, 1 sans id
        assertEquals(3, plants.size)
    }

    @Test
    fun `une categorie inconnue est ignoree sans faire echouer la fiche`() {
        val plant = plants.first { it.id == "plante_complete" }
        assertEquals(listOf(PlantSymptomCategory.DIGESTION_TRANSIT, PlantSymptomCategory.REGLES_CYCLE), plant.symptomCategories)
    }

    @Test
    fun `une fiche complete est correctement parsee`() {
        val plant = plants.first { it.id == "plante_complete" }

        assertEquals("Plante complète", plant.name)
        assertEquals("Plantus completus", plant.latinName)
        assertEquals("Symptôme principal test", plant.mainSymptom)
        assertEquals(listOf("Symptôme A", "Symptôme B"), plant.targetSymptoms)
        assertEquals(listOf("Feuilles"), plant.partsUsed)
        assertEquals(listOf("Antispasmodique"), plant.properties)
        assertEquals(listOf("Infusion", "TM"), plant.galenicForms)
        assertEquals("3g", plant.maxDailyDose)
        assertEquals(listOf("Grossesse"), plant.contraindications)
    }

    @Test
    fun `les champs optionnels absents deviennent des listes vides ou null, pas un defaut Kotlin errone`() {
        val plant = plants.first { it.id == "plante_minimale" }

        assertNull(plant.latinName)
        assertNull(plant.maxDailyDose)
        assertTrue(plant.targetSymptoms.isEmpty())
        assertTrue(plant.partsUsed.isEmpty())
        assertTrue(plant.properties.isEmpty())
        assertTrue(plant.galenicForms.isEmpty())
        assertTrue(plant.contraindications.isEmpty())
        assertTrue(plant.symptomCategories.isEmpty())
    }
}
