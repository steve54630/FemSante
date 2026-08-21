package com.audreyRetournayDiet.femSante.data.recommendation

import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.room.type.PainZone
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste la **reconstruction des tags typés** par [ContentTagJsonParser] sur un mini-fixture dédié
 * (`test/resources/content_tags_sample.json`) — pas la vraie table de prod (destinée à passer côté
 * API). On valide que chaque type de tag et chaque flag est correctement reconstruit depuis sa
 * chaîne.
 */
class ContentTagJsonParserTest {

    private val catalog: Map<ContentRef, Set<JournalTag>> by lazy {
        val json = javaClass.getResourceAsStream("/content_tags_sample.json")!!
            .bufferedReader().use { it.readText() }
        ContentTagJsonParser.parse(json)
    }

    @Test
    fun `parse toutes les entrees du fixture`() {
        assertEquals(4, catalog.size)
    }

    @Test
    fun `les types de contenu sont resolus`() {
        assertTrue(ContentRef(ContentType.PDF, "sample_pdf") in catalog)
        assertTrue(ContentRef(ContentType.VIDEO, "sample_video") in catalog)
        assertTrue(ContentRef(ContentType.AUDIO, "sample_audio") in catalog)
        assertTrue(ContentRef(ContentType.TOOLBOX, "sample_toolbox") in catalog)
    }

    @Test
    fun `zones et flags reconstruits pour la fiche outil`() {
        val tags = catalog.getValue(ContentRef(ContentType.TOOLBOX, "sample_toolbox"))
        assertEquals(setOf(JournalTag.Zone(PainZone.BASSIN), JournalTag.Sos), tags)
    }

    @Test
    fun `zones et flags reconstruits pour le pdf`() {
        val tags = catalog.getValue(ContentRef(ContentType.PDF, "sample_pdf"))
        assertEquals(
            setOf(JournalTag.Zone(PainZone.ABDOMEN), JournalTag.Digestif),
            tags
        )
    }

    @Test
    fun `causes qualites activites et flags reconstruits pour la video`() {
        val tags = catalog.getValue(ContentRef(ContentType.VIDEO, "sample_video"))
        assertEquals(
            setOf(
                JournalTag.Cause(DifficultyCause.STRESS),
                JournalTag.Quality(DayQuality.MOYENNE),
                JournalTag.Activity(PhysicalActivity.REPOS),
                JournalTag.EmotionallementDifficile,
                JournalTag.Sos
            ),
            tags
        )
    }

    @Test
    fun `une entree sans tags donne un ensemble vide`() {
        assertTrue(catalog.getValue(ContentRef(ContentType.AUDIO, "sample_audio")).isEmpty())
    }
}
