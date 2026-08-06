package com.audreyRetournayDiet.femSante.data.report

import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import com.audreyRetournayDiet.femSante.room.type.PainZone
import java.time.LocalDate

/** Période couverte par le récap (à partir d'aujourd'hui, en arrière). */
enum class ReportPeriod(val months: Long) {
    ONE_MONTH(1),
    THREE_MONTHS(3),
    SIX_MONTHS(6)
}

/** Niveau de détail du PDF. */
enum class ReportFormat {
    /** Synthèse uniquement (agrégats). */
    SUMMARY_ONLY,

    /** Synthèse + journal chronologique jour par jour. */
    SUMMARY_AND_JOURNAL
}

/**
 * Récap médical **agrégé localement** du suivi (journal + cycle) sur une période. Purement
 * factuel (ce que l'utilisatrice a saisi), sans interprétation — destiné à être présenté à
 * un professionnel de santé. Jamais envoyé au backend.
 */
data class MedicalReport(
    val from: LocalDate,
    val to: LocalDate,
    val cycle: CycleSummary,
    val symptoms: SymptomSummary,
    val sleep: SleepSummary,
    val measurements: MeasurementSummary,
    /** Journal chronologique (jours saisis uniquement), le plus récent en dernier. */
    val days: List<DayLine>
)

data class CycleSummary(
    val profile: CycleProfile,
    /** Longueur moyenne réelle des cycles si ≥ 2 débuts de règles, sinon null. */
    val averageCycleLength: Int?,
    val periodDaysCount: Int,
    val periodStartsCount: Int
)

data class SymptomSummary(
    val loggedDaysCount: Int,
    val averagePain: Double?,
    val maxPain: Int?,
    /** Zones de douleur les plus fréquentes, décroissant (zone → nombre de jours). */
    val topPainZones: List<Pair<PainZone, Int>>,
    val nauseaDaysCount: Int,
    val medicationDaysCount: Int
)

/** Une ligne du journal chronologique (un jour ayant au moins une saisie journal ou cycle). */
data class DayLine(
    val date: LocalDate,
    val painLevel: Int?,
    val zones: List<PainZone>,
    val nausea: Boolean,
    val isPeriod: Boolean,
    val notes: String?,
    /** Durée de sommeil du jour en minutes, si coucher + réveil renseignés. */
    val sleepDurationMinutes: Int? = null
)

/** Synthèse du sommeil sur la période. */
data class SleepSummary(
    val loggedNights: Int,
    /** Durée moyenne en minutes, ou null si aucune nuit renseignée. */
    val averageDurationMinutes: Int?
)

/** Mesures suivies (une métrique par ligne présente dans les saisies). */
enum class MeasurementMetric { WEIGHT, WAIST, HIPS, THIGHS, CHEST, ARMS }

/** Évolution des mesures : pour chaque métrique, valeur de début et de fin sur la période. */
data class MeasurementSummary(
    val trends: List<MetricTrend>
)

data class MetricTrend(
    val metric: MeasurementMetric,
    val firstDate: LocalDate,
    val firstValue: Double,
    val lastDate: LocalDate,
    val lastValue: Double,
    /** Tous les points chronologiques (pour tracer la courbe). */
    val points: List<MeasurePoint>
)

data class MeasurePoint(val date: LocalDate, val value: Double)
