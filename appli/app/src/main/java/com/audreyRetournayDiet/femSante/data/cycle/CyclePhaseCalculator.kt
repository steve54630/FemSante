package com.audreyRetournayDiet.femSante.data.cycle

import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Estime la phase du cycle pour une date donnée, à partir de l'historique des jours de
 * règles. Logique purement déclarative et sans prédiction (incrément 2) : on ne calcule
 * **jamais** de date future ici.
 *
 * Paramètres (valeurs par défaut validées côté dev) :
 * - longueur de cycle = moyenne réelle de l'historique, sinon [DEFAULT_CYCLE_LENGTH] ;
 *   bornée à [MIN_CYCLE_LENGTH]..[MAX_CYCLE_LENGTH] pour rester plausible ;
 * - durée des règles = [PERIOD_LENGTH] jours ;
 * - phase lutéale = [LUTEAL_LENGTH] jours (constante physiologique usuelle) ;
 * - au-delà de [MAX_REASONABLE_GAP] jours depuis les dernières règles → [CyclePhase.INDETERMINEE]
 *   (évite d'afficher une phase fausse, notamment en cycle irrégulier).
 */
object CyclePhaseCalculator {

    private const val DEFAULT_CYCLE_LENGTH = 28
    private const val MIN_CYCLE_LENGTH = 21
    private const val MAX_CYCLE_LENGTH = 35
    private const val PERIOD_LENGTH = 5
    private const val LUTEAL_LENGTH = 14
    private const val MAX_REASONABLE_GAP = 45

    /**
     * @return la phase estimée, ou `null` si aucune phase ne doit être affichée
     *         (profil [CycleProfile.ABSENT_OU_PILULE]).
     */
    fun calculate(
        periodDates: Set<LocalDate>,
        target: LocalDate,
        profile: CycleProfile
    ): CyclePhase? {
        // Profil sans cycle exploitable : pas d'affichage de phase.
        if (profile == CycleProfile.ABSENT_OU_PILULE) return null

        val starts = periodStarts(periodDates)
        val lastStart = starts.filter { !it.isAfter(target) }.maxOrNull()
            ?: return CyclePhase.INDETERMINEE

        val daysSinceStart = ChronoUnit.DAYS.between(lastStart, target).toInt() // 0 = 1er jour
        if (daysSinceStart > MAX_REASONABLE_GAP) return CyclePhase.INDETERMINEE

        val cycleLength = averageCycleLength(starts)
        val ovulationIndex = cycleLength - LUTEAL_LENGTH // index 0-based du jour d'ovulation

        return when {
            daysSinceStart < PERIOD_LENGTH -> CyclePhase.MENSTRUELLE
            daysSinceStart < ovulationIndex - 1 -> CyclePhase.FOLLICULAIRE
            daysSinceStart <= ovulationIndex + 1 -> CyclePhase.OVULATION
            else -> CyclePhase.LUTEALE
        }
    }

    /** Un « début de règles » = un jour de règles dont la veille n'est pas un jour de règles. */
    private fun periodStarts(periodDates: Set<LocalDate>): List<LocalDate> =
        periodDates.filter { it.minusDays(1) !in periodDates }.sorted()

    private fun averageCycleLength(starts: List<LocalDate>): Int {
        if (starts.size < 2) return DEFAULT_CYCLE_LENGTH
        val gaps = starts.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b).toInt() }
        val average = gaps.average().toInt()
        return average.coerceIn(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH)
    }
}
