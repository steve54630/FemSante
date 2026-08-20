package com.audreyRetournayDiet.femSante.data.micronutrient

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste la **logique de parsing** des micronutriments sur des mini-fixtures dédiés
 * (`test/resources/micronutrients_sample.json` et `nutrient_interactions_sample.json`).
 *
 * On ne parse **pas** les vrais assets de prod (susceptibles de passer côté API) : le fixture ne
 * valide que le parseur (champs, résolution de l'enum de groupe, unicité des identifiants).
 */
class MicronutrientJsonParsingTest {

    private fun fixture(name: String): String =
        javaClass.getResourceAsStream("/$name")!!.bufferedReader().use { it.readText() }

    private val nutrients: List<Micronutrient> by lazy {
        MicronutrientJsonParser.parseNutrients(fixture("micronutrients_sample.json"))
    }
    private val interactions: List<DrugInteraction> by lazy {
        MicronutrientJsonParser.parseInteractions(fixture("nutrient_interactions_sample.json"))
    }

    @Test
    fun `parse les fiches et les interactions du fixture`() {
        assertEquals(4, nutrients.size)
        assertEquals(2, interactions.size)
        interactions.forEach {
            assertTrue("médicament vide", it.drug.isNotBlank())
            assertTrue("nutriments vides pour '${it.drug}'", it.nutrients.isNotBlank())
        }
    }

    @Test
    fun `chaque fiche a les champs essentiels renseignes`() {
        nutrients.forEach { n ->
            assertTrue("id vide", n.id.isNotBlank())
            assertTrue("nom vide pour '${n.id}'", n.name.isNotBlank())
            assertTrue("unité vide pour '${n.id}'", n.unit.isNotBlank())
            assertTrue("aucune source pour '${n.id}'", n.sources.isNotEmpty())
            assertTrue("apport femme vide pour '${n.id}'", n.intake.women.isNotBlank())
            n.sources.forEach { s ->
                assertTrue("nom de source vide dans '${n.id}'", s.name.isNotBlank())
                assertTrue("teneur vide dans '${n.id}'", s.amount.isNotBlank())
            }
        }
    }

    @Test
    fun `les groupes sont resolus par nom`() {
        // Gson mettrait le groupe à null si le libellé ne correspondait à aucune valeur d'enum.
        assertTrue(nutrients.all { it.group != null })
        assertEquals(
            setOf(
                NutrientGroup.LIPOSOLUBLE,
                NutrientGroup.HYDROSOLUBLE,
                NutrientGroup.MINERAL,
                NutrientGroup.OLIGO
            ),
            nutrients.map { it.group }.toSet()
        )
    }

    @Test
    fun `les identifiants sont uniques`() {
        val ids = nutrients.map { it.id }
        assertEquals("des identifiants sont dupliqués", ids.size, ids.toSet().size)
    }
}
