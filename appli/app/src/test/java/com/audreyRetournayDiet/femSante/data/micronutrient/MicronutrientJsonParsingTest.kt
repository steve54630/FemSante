package com.audreyRetournayDiet.femSante.data.micronutrient

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Valide les fichiers réellement livrés `assets/micronutrients.json` et
 * `assets/nutrient_interactions.json` (pas un échantillon), sans `Context`/`AssetManager`.
 *
 * Objectif : détecter au plus tôt une virgule manquante, un groupe invalide ou un champ vide,
 * avant même de brancher l'UI. Même approche que `RecipeJsonParsingTest`.
 */
class MicronutrientJsonParsingTest {

    private fun loadAsset(name: String): String {
        val candidates = listOf("src/main/assets/$name", "app/src/main/assets/$name")
        val file = candidates.map(::File).firstOrNull { it.exists() }
            ?: error("$name introuvable (répertoire courant : ${File("").absolutePath})")
        return file.readText()
    }

    private val nutrients: List<Micronutrient> by lazy {
        MicronutrientJsonParser.parseNutrients(loadAsset("micronutrients.json"))
    }
    private val interactions: List<DrugInteraction> by lazy {
        MicronutrientJsonParser.parseInteractions(loadAsset("nutrient_interactions.json"))
    }

    @Test
    fun `le fichier contient les 22 fiches`() {
        assertEquals(22, nutrients.size)
    }

    @Test
    fun `le fichier contient les 12 interactions`() {
        assertEquals(12, interactions.size)
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
            assertTrue("apport enceinte vide pour '${n.id}'", n.intake.pregnant.isNotBlank())
            n.sources.forEach { s ->
                assertTrue("nom de source vide dans '${n.id}'", s.name.isNotBlank())
                assertTrue("teneur vide dans '${n.id}'", s.amount.isNotBlank())
            }
        }
    }

    @Test
    fun `les identifiants sont uniques`() {
        val ids = nutrients.map { it.id }
        assertEquals("des identifiants sont dupliqués", ids.size, ids.toSet().size)
    }

    @Test
    fun `les groupes sont valides et correctement repartis`() {
        // Gson mettrait le groupe à null si le libellé ne correspondait à aucune valeur de l'enum.
        assertTrue(nutrients.all { it.group != null })
        val countByGroup = nutrients.groupingBy { it.group }.eachCount()
        assertEquals(4, countByGroup[NutrientGroup.LIPOSOLUBLE])
        assertEquals(9, countByGroup[NutrientGroup.HYDROSOLUBLE])
        assertEquals(5, countByGroup[NutrientGroup.MINERAL])
        assertEquals(4, countByGroup[NutrientGroup.OLIGO])
    }
}
