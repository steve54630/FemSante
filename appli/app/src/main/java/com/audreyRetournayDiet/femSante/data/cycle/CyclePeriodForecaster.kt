package com.audreyRetournayDiet.femSante.data.cycle

import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Prévisionnel des règles (incrément 3). Purement calculé à la volée à partir des vraies
 * dates de règles et des paramètres déclarés — **jamais** persisté. Une prédiction ne peut
 * donc pas polluer l'historique servant aux calculs futurs (aucune boucle de rétroaction).
 *
 * Garde-fou « zéro anxiété » : on ne projette que pour un profil [CycleProfile.REGULIER].
 * Pour [CycleProfile.IRREGULIER] et [CycleProfile.ABSENT_OU_PILULE], aucune prévision.
 *
 * Paramètres (cohérents avec [CyclePhaseCalculator]) :
 * - longueur de cycle = moyenne réelle de l'historique si ≥ 2 débuts, sinon valeur
 *   déclarée [cycleLength] ; bornée à [MIN_CYCLE_LENGTH]..[MAX_CYCLE_LENGTH] ;
 * - durée des règles = [periodLength] jours ;
 * - [horizonCycles] : nombre de cycles à venir projetés (par défaut [DEFAULT_HORIZON_CYCLES]).
 */
object CyclePeriodForecaster {

    private const val DEFAULT_CYCLE_LENGTH = 28
    private const val DEFAULT_PERIOD_LENGTH = 5
    private const val MIN_CYCLE_LENGTH = 21
    private const val MAX_CYCLE_LENGTH = 35
    private const val DEFAULT_HORIZON_CYCLES = 3

    /**
     * @return les jours de règles prédits pour les prochains cycles, ou un ensemble vide
     *         si le profil n'est pas régulier ou si aucun historique n'est exploitable.
     */
    fun predictPeriodDates(
        periodDates: Set<LocalDate>,
        profile: CycleProfile,
        cycleLength: Int = DEFAULT_CYCLE_LENGTH,
        periodLength: Int = DEFAULT_PERIOD_LENGTH,
        horizonCycles: Int = DEFAULT_HORIZON_CYCLES
    ): Set<LocalDate> {
        // Garde-fou : prévisions uniquement pour les cycles réguliers.
        if (profile != CycleProfile.REGULIER) return emptySet()

        val starts = periodStarts(periodDates)
        val lastStart = starts.maxOrNull() ?: return emptySet()

        val effectiveCycle = averageCycleLength(starts, cycleLength)
        val effectivePeriod = periodLength.coerceIn(1, effectiveCycle)

        val predicted = mutableSetOf<LocalDate>()
        for (cycleIndex in 1..horizonCycles) {
            val windowStart = lastStart.plusDays(effectiveCycle.toLong() * cycleIndex)
            for (dayOffset in 0 until effectivePeriod) {
                predicted.add(windowStart.plusDays(dayOffset.toLong()))
            }
        }

        // Une prévision ne recouvre jamais un jour déjà réellement saisi comme règles.
        return predicted - periodDates
    }

    /** Un « début de règles » = un jour de règles dont la veille n'est pas un jour de règles. */
    private fun periodStarts(periodDates: Set<LocalDate>): List<LocalDate> =
        periodDates.filter { it.minusDays(1) !in periodDates }.sorted()

    /** Moyenne réelle des écarts si l'historique le permet, sinon la valeur déclarée. */
    private fun averageCycleLength(starts: List<LocalDate>, declared: Int): Int {
        if (starts.size < 2) return declared.coerceIn(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH)
        val gaps = starts.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
        return gaps.average().toInt().coerceIn(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH)
    }
}
