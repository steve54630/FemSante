package com.audreyRetournayDiet.femSante.data.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests du filtrage pur des contenus média, sur le catalogue « tête » réel.
 */
class MediaFilterTest {

    private val tete = MediaCatalog.tete

    @Test
    fun `le catalogue tete contient 9 videos et 8 audios`() {
        assertEquals(9, tete.count { it.type == MediaType.VIDEO })
        assertEquals(8, tete.count { it.type == MediaType.AUDIO })
    }

    @Test
    fun `les themes proposes dependent du type`() {
        assertEquals(
            listOf(MediaCategory.ART_THERAPIE, MediaCategory.SOPHROLOGIE, MediaCategory.EMOTIONS),
            MediaFilter.categoriesFor(tete, MediaType.VIDEO)
        )
        assertEquals(
            listOf(MediaCategory.SOPHROLOGIE, MediaCategory.MEDITATION, MediaCategory.HYPNOSE),
            MediaFilter.categoriesFor(tete, MediaType.AUDIO)
        )
    }

    @Test
    fun `filtre type seul`() {
        val videos = MediaFilter.filter(tete, MediaType.VIDEO, category = null)
        assertEquals(9, videos.size)
        assertTrue(videos.all { it.type == MediaType.VIDEO })
    }

    @Test
    fun `filtre type plus theme`() {
        assertEquals(4, MediaFilter.filter(tete, MediaType.VIDEO, MediaCategory.ART_THERAPIE).size)
        assertEquals(4, MediaFilter.filter(tete, MediaType.AUDIO, MediaCategory.MEDITATION).size)
        assertEquals(2, MediaFilter.filter(tete, MediaType.AUDIO, MediaCategory.HYPNOSE).size)
    }

    @Test
    fun `la sophrologie existe en video et en audio`() {
        assertEquals(4, MediaFilter.filter(tete, MediaType.VIDEO, MediaCategory.SOPHROLOGIE).size)
        assertEquals(2, MediaFilter.filter(tete, MediaType.AUDIO, MediaCategory.SOPHROLOGIE).size)
    }

    @Test
    fun `un theme absent d'un type renvoie une liste vide`() {
        // La méditation n'existe qu'en audio.
        assertTrue(MediaFilter.filter(tete, MediaType.VIDEO, MediaCategory.MEDITATION).isEmpty())
    }
}
