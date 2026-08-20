package com.audreyRetournayDiet.femSante.data.resource

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Teste [ResourceDocumentJsonParser] sur un mini-fixture dédié
 * (`test/resources/resources_sample.json`) — pas les vrais assets de prod. Valide le repli de
 * l'`id` sur le nom de PDF et le rejet des entrées sans titre.
 */
class ResourceDocumentJsonParserTest {

    private val documents: List<ResourceDocument> by lazy {
        val json = javaClass.getResourceAsStream("/resources_sample.json")!!
            .bufferedReader().use { it.readText() }
        ResourceDocumentJsonParser.parse(json)
    }

    @Test
    fun `les entrees sans titre sont ignorees`() {
        assertEquals(2, documents.size)
    }

    @Test
    fun `l'id se deduit du pdf quand il est absent`() {
        val b = documents.first { it.pdf == "sample_b.pdf" }
        assertEquals("sample_b", b.id)
        assertEquals("Fiche B sans id", b.title)
    }

    @Test
    fun `les champs complets sont conserves`() {
        val a = documents.first { it.id == "sample_a" }
        assertEquals("sample_a.pdf", a.pdf)
        assertEquals("Fiche A", a.title)
    }
}
