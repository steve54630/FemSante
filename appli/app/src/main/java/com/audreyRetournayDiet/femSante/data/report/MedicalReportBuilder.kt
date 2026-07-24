package com.audreyRetournayDiet.femSante.data.report

import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Construit un [MedicalReport] à partir des données brutes (journal + cycle) d'une période.
 *
 * Logique **pure** (aucun accès base ni Android) : les lectures sont faites par l'appelant
 * et les résultats passés en paramètres, ce qui rend l'agrégation testable et garantit
 * qu'aucune donnée ne transite ailleurs qu'en local. Purement factuel, sans interprétation.
 */
object MedicalReportBuilder {

    fun build(
        from: LocalDate,
        to: LocalDate,
        entries: List<DailyEntryFull>,
        cycleDays: List<CycleDayEntity>,
        profile: CycleProfile
    ): MedicalReport {
        val zone = ZoneId.systemDefault()
        fun toDate(timestamp: Long): LocalDate =
            Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate()

        val journalByDate = entries.associateBy { toDate(it.dailyEntry.date) }
        val cycleByDate = cycleDays.associateBy { toDate(it.date) }

        // --- Synthèse symptômes ---
        val pains = entries.mapNotNull { it.generalState?.painLevel }
        val zoneCounts = entries
            .flatMap { it.symptomsState?.localizedPains ?: emptyList() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { it.key to it.value }

        val symptoms = SymptomSummary(
            loggedDaysCount = entries.size,
            averagePain = pains.average().takeIf { pains.isNotEmpty() },
            maxPain = pains.maxOrNull(),
            topPainZones = zoneCounts,
            nauseaDaysCount = entries.count { it.symptomsState?.hasNausea == true },
            medicationDaysCount = entries.count { it.contextState?.medecineTaken == true }
        )

        // --- Synthèse cycle ---
        val periodDates = cycleDays.filter { it.isPeriod }.map { toDate(it.date) }.toSortedSet()
        val periodStarts = periodDates.filter { it.minusDays(1) !in periodDates }.sorted()
        val averageCycleLength = if (periodStarts.size >= 2) {
            periodStarts.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }.average().toInt()
        } else null

        val cycle = CycleSummary(
            profile = profile,
            averageCycleLength = averageCycleLength,
            periodDaysCount = periodDates.size,
            periodStartsCount = periodStarts.size
        )

        // --- Journal chronologique (union des jours saisis en journal ou en cycle) ---
        val days = (journalByDate.keys + cycleByDate.keys).toSortedSet().map { date ->
            val entry = journalByDate[date]
            DayLine(
                date = date,
                painLevel = entry?.generalState?.painLevel,
                zones = entry?.symptomsState?.localizedPains ?: emptyList(),
                nausea = entry?.symptomsState?.hasNausea == true,
                isPeriod = cycleByDate[date]?.isPeriod == true,
                notes = entry?.symptomsState?.others?.takeIf { it.isNotBlank() }
            )
        }

        return MedicalReport(from = from, to = to, cycle = cycle, symptoms = symptoms, days = days)
    }
}
