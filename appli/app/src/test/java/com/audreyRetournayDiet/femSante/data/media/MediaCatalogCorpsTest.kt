package com.audreyRetournayDiet.femSante.data.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests du catalogue « corps » réel : contenu vidéo uniquement, réparti en Yoga / Pilates /
 * Fitness, avec « Débutant au Yoga » comme seule séance gratuite.
 */
class MediaCatalogCorpsTest {

    private val corps = MediaCatalog.corps

    @Test
    fun `le catalogue corps ne contient que des videos`() {
        assertEquals(5, corps.size)
        assertTrue(corps.all { it.type == MediaType.VIDEO })
    }

    @Test
    fun `aucune video corps n'a de support pdf`() {
        assertTrue(corps.none { it.pdf })
    }

    @Test
    fun `les themes corps sont yoga pilates fitness dans cet ordre`() {
        assertEquals(
            listOf(MediaCategory.YOGA, MediaCategory.PILATES, MediaCategory.FITNESS),
            MediaFilter.categoriesFor(corps, MediaType.VIDEO)
        )
    }

    @Test
    fun `le yoga regroupe trois seances`() {
        assertEquals(3, MediaFilter.filter(corps, MediaType.VIDEO, MediaCategory.YOGA).size)
        assertEquals(1, MediaFilter.filter(corps, MediaType.VIDEO, MediaCategory.PILATES).size)
        assertEquals(1, MediaFilter.filter(corps, MediaType.VIDEO, MediaCategory.FITNESS).size)
    }

    @Test
    fun `seul debutant au yoga est gratuit`() {
        val gratuits = corps.filterNot { it.premium }
        assertEquals(listOf("Débutant au Yoga"), gratuits.map { it.title })
        assertTrue(corps.first { it.title == "SOS Douleurs" }.premium)
    }
}
