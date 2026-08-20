package com.audreyRetournayDiet.femSante.data.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste le **filtrage pur** ([MediaFilter]) sur le mini-fixture dédié — pas le vrai catalogue,
 * pour ne pas coupler les tests à la donnée de prod (cf. [MediaJsonParsingTest]).
 */
class MediaFilterTest {

    private val all: List<MediaItem> by lazy {
        val json = javaClass.getResourceAsStream("/media_sample.json")!!
            .bufferedReader().use { it.readText() }
        MediaJsonParser.parse(json)
    }
    private val tete get() = all.filter { it.module == MediaModule.TETE }
    private val corps get() = all.filter { it.module == MediaModule.CORPS }

    @Test
    fun `filtre par type`() {
        assertEquals(2, MediaFilter.filter(tete, MediaType.VIDEO, category = null).size)
        assertEquals(2, MediaFilter.filter(tete, MediaType.AUDIO, category = null).size)
    }

    @Test
    fun `les themes proposes dependent du type`() {
        assertEquals(
            listOf(MediaCategory.ART_THERAPIE, MediaCategory.SOPHROLOGIE),
            MediaFilter.categoriesFor(tete, MediaType.VIDEO)
        )
        assertEquals(
            listOf(MediaCategory.SOPHROLOGIE, MediaCategory.MEDITATION),
            MediaFilter.categoriesFor(tete, MediaType.AUDIO)
        )
        assertEquals(
            listOf(MediaCategory.YOGA, MediaCategory.PILATES),
            MediaFilter.categoriesFor(corps, MediaType.VIDEO)
        )
    }

    @Test
    fun `la sophrologie existe en video et en audio`() {
        assertEquals(1, MediaFilter.filter(tete, MediaType.VIDEO, MediaCategory.SOPHROLOGIE).size)
        assertEquals(1, MediaFilter.filter(tete, MediaType.AUDIO, MediaCategory.SOPHROLOGIE).size)
    }

    @Test
    fun `un theme absent d'un type renvoie une liste vide`() {
        // La méditation n'existe qu'en audio dans le fixture.
        assertTrue(MediaFilter.filter(tete, MediaType.VIDEO, MediaCategory.MEDITATION).isEmpty())
    }
}
