package com.audreyRetournayDiet.femSante.data.toolbox

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste [ToolboxFileJsonParser] sur un mini-fixture dédié (`test/resources/toolbox_file_sample.json`) —
 * pas les vrais assets de prod. Valide le mapping des champs, la gestion des listes optionnelles,
 * le fallback d'auteur et l'exclusion des catégories inconnues ou fiches corrompues.
 */
class ToolboxFileJsonParserTest {

    private val adviceList: List<ToolboxAdvice> by lazy {
        val json = javaClass.getResourceAsStream("/toolbox_file_sample.json")!!
            .bufferedReader().use { it.readText() }
        ToolboxFileJsonParser.parse(json)
    }

    @Test
    fun `les fiches corrompues ou de categorie inconnue sont ignorees`() {
        // Le sample contient 4 entrées : 2 valides, 1 avec catégorie invalide, 1 sans titre
        assertEquals(2, adviceList.size)
    }

    @Test
    fun `une fiche complete est correctement parsee`() {
        val massage = adviceList.first { it.id == "massage_test" }

        assertEquals("AUTOMASSAGE TEST", massage.title)
        assertEquals(ToolboxCategory.MASSAGE, massage.category)
        assertEquals("Priscillia Devaux", massage.author.name)
        assertEquals("Naturopathe certifiée", massage.author.role)
        assertEquals("Résumé court.", massage.summary)

        // Items
        assertEquals(1, massage.items.size)
        val item = massage.items.first()
        assertEquals("Huile de sésame", item.name)
        assertNull(item.latinName)
        assertEquals(listOf("Stimulante"), item.properties)
        assertTrue(item.contraindications.isEmpty())

        // Protocols
        assertEquals(1, massage.protocols.size)
        val protocol = massage.protocols.first()
        assertEquals("Transit lent", protocol.indication)
        assertEquals("Ventre", protocol.applicationSite)
        assertEquals(listOf("Etape 1", "Etape 2"), protocol.steps)

        // Precautions
        assertEquals(listOf("Usage externe uniquement."), massage.precautions)
    }

    @Test
    fun `les champs optionnels ou vides sont geres proprement`() {
        val phytotherapy = adviceList.first { it.id == "phyto_test" }

        assertEquals(ToolboxCategory.PHYTOTHERAPY, phytotherapy.category)

        // Item avec nom latin et contre-indications
        val plant = phytotherapy.items.first()
        assertEquals("Menthe poivrée", plant.name)
        assertEquals("Mentha x piperita", plant.latinName)
        assertEquals(listOf("Grossesse"), plant.contraindications)
    }
}