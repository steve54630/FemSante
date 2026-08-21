package com.audreyRetournayDiet.femSante.data.report

import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.BodyMeasurementEntity
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

        val sleepDurations = entries.mapNotNull {
            sleepMinutes(it.generalState?.bedTimeMinutes, it.generalState?.wakeTimeMinutes)
        }
        val sleep = SleepSummary(
            loggedNights = sleepDurations.size,
            averageDurationMinutes = if (sleepDurations.isEmpty()) null else sleepDurations.average().toInt()
        )

        val trends = MeasurementMetric.values().mapNotNull { metric ->
            val samples = entries
                .mapNotNull { e -> metricValue(e.measurement, metric)?.let { toDate(e.dailyEntry.date) to it } }
                .sortedBy { it.first }
            if (samples.isEmpty()) null
            else MetricTrend(
                metric = metric,
                firstDate = samples.first().first,
                firstValue = samples.first().second,
                lastDate = samples.last().first,
                lastValue = samples.last().second,
                points = samples.map { MeasurePoint(it.first, it.second) }
            )
        }
        val measurements = MeasurementSummary(trends)

        // --- Journal chronologique (union des jours saisis en journal ou en cycle) ---
        val days = (journalByDate.keys + cycleByDate.keys).toSortedSet().map { date ->
            val entry = journalByDate[date]
            DayLine(
                date = date,
                painLevel = entry?.generalState?.painLevel,
                zones = entry?.symptomsState?.localizedPains ?: emptyList(),
                nausea = entry?.symptomsState?.hasNausea == true,
                isPeriod = cycleByDate[date]?.isPeriod == true,
                notes = entry?.symptomsState?.others?.takeIf { it.isNotBlank() },
                sleepDurationMinutes = sleepMinutes(entry?.generalState?.bedTimeMinutes, entry?.generalState?.wakeTimeMinutes)
            )
        }

        return MedicalReport(
            from = from, to = to, cycle = cycle, symptoms = symptoms,
            sleep = sleep, measurements = measurements, days = days
        )
    }

    /** Durée de sommeil en minutes (gère le passage de minuit), ou null si incomplet. */
    private fun sleepMinutes(bed: Int?, wake: Int?): Int? {
        if (bed == null || wake == null) return null
        var minutes = wake - bed
        if (minutes <= 0) minutes += 24 * 60
        return minutes
    }

    private fun metricValue(m: BodyMeasurementEntity?, metric: MeasurementMetric): Double? = when (metric) {
        MeasurementMetric.WEIGHT -> m?.weightKg
        MeasurementMetric.WAIST -> m?.waistCm
        MeasurementMetric.HIPS -> m?.hipsCm
        MeasurementMetric.THIGHS -> m?.thighsCm
        MeasurementMetric.CHEST -> m?.chestCm
        MeasurementMetric.ARMS -> m?.armsCm
    }
}
