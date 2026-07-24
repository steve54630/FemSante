package com.audreyRetournayDiet.femSante.data.report

import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import com.audreyRetournayDiet.femSante.room.type.PainZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Tests de l'agrégation du récap médical (logique pure, JVM).
 */
class MedicalReportBuilderTest {

    private val userId = "u1"
    private val d1 = LocalDate.of(2024, 6, 1)

    private fun ts(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun entry(date: LocalDate, pain: Int, zones: List<PainZone>, nausea: Boolean, notes: String? = null) =
        DailyEntryFull(
            dailyEntry = DailyEntryEntity(userId = userId, date = ts(date)),
            generalState = GeneralStateEntity(entryId = 0L, painLevel = pain),
            psychologicalState = null,
            symptomsState = SymptomStateEntity(entryId = 0L, localizedPains = zones, hasNausea = nausea, others = notes ?: ""),
            contextState = null
        )

    @Test
    fun `synthese symptomes correcte`() {
        val entries = listOf(
            entry(d1, pain = 6, zones = listOf(PainZone.BASSIN), nausea = true, notes = "jour difficile"),
            entry(d1.plusDays(3), pain = 2, zones = listOf(PainZone.BASSIN, PainZone.LOMBAIRES), nausea = false)
        )

        val report = MedicalReportBuilder.build(
            from = d1, to = d1.plusDays(40),
            entries = entries, cycleDays = emptyList(), profile = CycleProfile.REGULIER
        )

        assertEquals(2, report.symptoms.loggedDaysCount)
        assertEquals(4.0, report.symptoms.averagePain!!, 0.001)
        assertEquals(6, report.symptoms.maxPain)
        assertEquals(1, report.symptoms.nauseaDaysCount)
        // Le bassin apparaît 2 fois -> zone la plus fréquente.
        assertEquals(PainZone.BASSIN to 2, report.symptoms.topPainZones.first())
    }

    @Test
    fun `synthese cycle avec deux debuts donne une longueur moyenne`() {
        val cycleDays = listOf(
            CycleDayEntity(userId = userId, date = ts(d1), isPeriod = true),
            CycleDayEntity(userId = userId, date = ts(d1.plusDays(28)), isPeriod = true)
        )

        val report = MedicalReportBuilder.build(
            from = d1, to = d1.plusDays(40),
            entries = emptyList(), cycleDays = cycleDays, profile = CycleProfile.REGULIER
        )

        assertEquals(CycleProfile.REGULIER, report.cycle.profile)
        assertEquals(2, report.cycle.periodDaysCount)
        assertEquals(2, report.cycle.periodStartsCount)
        assertEquals(28, report.cycle.averageCycleLength)
    }

    @Test
    fun `un seul debut de regles ne donne pas de moyenne`() {
        val cycleDays = listOf(CycleDayEntity(userId = userId, date = ts(d1), isPeriod = true))

        val report = MedicalReportBuilder.build(
            from = d1, to = d1.plusDays(40),
            entries = emptyList(), cycleDays = cycleDays, profile = CycleProfile.IRREGULIER
        )

        assertNull(report.cycle.averageCycleLength)
        assertEquals(1, report.cycle.periodStartsCount)
    }

    @Test
    fun `le journal chronologique fusionne journal et cycle`() {
        val entries = listOf(entry(d1, pain = 5, zones = listOf(PainZone.TETE), nausea = false, notes = "note"))
        val cycleDays = listOf(
            CycleDayEntity(userId = userId, date = ts(d1), isPeriod = true),
            CycleDayEntity(userId = userId, date = ts(d1.plusDays(2)), isPeriod = false, spotting = true)
        )

        val report = MedicalReportBuilder.build(
            from = d1, to = d1.plusDays(40),
            entries = entries, cycleDays = cycleDays, profile = CycleProfile.REGULIER
        )

        // Union des dates saisies : d1 (journal + cycle) et d1+2 (cycle seul).
        assertEquals(2, report.days.size)
        val first = report.days.first { it.date == d1 }
        assertEquals(5, first.painLevel)
        assertEquals(listOf(PainZone.TETE), first.zones)
        assertTrue(first.isPeriod)
        assertEquals("note", first.notes)
    }
}
