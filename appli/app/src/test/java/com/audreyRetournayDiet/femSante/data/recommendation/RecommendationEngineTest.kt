package com.audreyRetournayDiet.femSante.data.recommendation

import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.PainZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la logique pure du moteur de recommandation.
 */
class RecommendationEngineTest {

    private val audioSos = ContentRef(ContentType.AUDIO, "audio_sos")
    private val videoBassin = ContentRef(ContentType.VIDEO, "video_bassin")
    private val pdfMauvaise = ContentRef(ContentType.PDF, "pdf_mauvaise.pdf")
    private val pdfDouble = ContentRef(ContentType.PDF, "pdf_double.pdf")
    private val pdfNeutre = ContentRef(ContentType.PDF, "pdf_neutre.pdf")

    private val catalog: Map<ContentRef, Set<JournalTag>> = mapOf(
        audioSos to setOf(JournalTag.Sos, JournalTag.Zone(PainZone.BASSIN)),
        videoBassin to setOf(JournalTag.Zone(PainZone.BASSIN)),
        pdfMauvaise to setOf(JournalTag.Quality(DayQuality.MAUVAISE)),
        pdfDouble to setOf(JournalTag.Zone(PainZone.BASSIN), JournalTag.Quality(DayQuality.MAUVAISE)),
        pdfNeutre to emptySet()
    )

    private fun entry(
        pain: Int,
        zones: List<PainZone> = emptyList(),
        quality: DayQuality = DayQuality.MOYENNE
    ) = DailyEntryFull(
        dailyEntry = DailyEntryEntity(userId = "u", date = 0L),
        generalState = GeneralStateEntity(entryId = 0L, painLevel = pain),
        psychologicalState = PsychologicalStateEntity(entryId = 0L, dayQuality = quality),
        symptomsState = SymptomStateEntity(entryId = 0L, localizedPains = zones),
        contextState = null
    )

    @Test
    fun `sans saisie tout le contenu est retourne`() {
        val result = RecommendationEngine.recommend(latestEntry = null, catalog = catalog)
        assertEquals(catalog.size, result.size)
    }

    @Test
    fun `mode SOS ne propose que le contenu de crise`() {
        // painLevel >= 6 -> seul le contenu tagué Sos
        val result = RecommendationEngine.recommend(entry(pain = 7, zones = listOf(PainZone.BASSIN)), catalog)
        assertEquals(listOf(audioSos), result.map { it.content })
    }

    @Test
    fun `logique OU un seul tag commun suffit et le contenu neutre est exclu`() {
        val result = RecommendationEngine.recommend(
            entry(pain = 2, zones = listOf(PainZone.BASSIN), quality = DayQuality.MAUVAISE),
            catalog
        )
        val contents = result.map { it.content }
        assertTrue(audioSos in contents)
        assertTrue(videoBassin in contents)
        assertTrue(pdfMauvaise in contents)
        assertTrue(pdfDouble in contents)
        // Contenu sans tag commun exclu
        assertTrue(pdfNeutre !in contents)
    }

    @Test
    fun `le contenu avec le plus de tags communs remonte en premier`() {
        val result = RecommendationEngine.recommend(
            entry(pain = 2, zones = listOf(PainZone.BASSIN), quality = DayQuality.MAUVAISE),
            catalog
        )
        // pdfDouble a 2 tags communs (Bassin + Mauvaise) -> premier malgré le type PDF
        assertEquals(pdfDouble, result.first().content)
        assertEquals(2, result.first().matchingTagCount)
    }

    @Test
    fun `a pertinence egale l'ordre est audio puis video puis pdf`() {
        // pain bas, un seul tag commun (Bassin) pour audioSos, videoBassin, pdfDouble n'est pas à égalité
        val result = RecommendationEngine.recommend(
            entry(pain = 2, zones = listOf(PainZone.BASSIN), quality = DayQuality.BONNE),
            catalog
        )
        // À 1 tag commun : audioSos (Bassin), videoBassin (Bassin), pdfDouble (Bassin) ; mauvaise non
        val oneMatch = result.filter { it.matchingTagCount == 1 }.map { it.content }
        // L'audio doit précéder la vidéo, qui précède le PDF
        assertTrue(oneMatch.indexOf(audioSos) < oneMatch.indexOf(videoBassin))
        assertTrue(oneMatch.indexOf(videoBassin) < oneMatch.indexOf(pdfDouble))
    }
}
