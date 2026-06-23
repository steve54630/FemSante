package com.audreyRetournayDiet.femSante.data.cycle

import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

/**
 * Tests de la logique pure d'estimation de phase (incrément 2).
 * Aucune dépendance Android : exécution JVM rapide.
 */
class CyclePhaseCalculatorTest {

    private val start = LocalDate.of(2024, 1, 1)

    @Test
    fun `profil absent ou pilule masque la phase`() {
        val result = CyclePhaseCalculator.calculate(setOf(start), start, CycleProfile.ABSENT_OU_PILULE)
        assertNull(result)
    }

    @Test
    fun `historique vide donne indeterminee`() {
        val result = CyclePhaseCalculator.calculate(emptySet(), start, CycleProfile.REGULIER)
        assertEquals(CyclePhase.INDETERMINEE, result)
    }

    @Test
    fun `pendant les regles la phase est menstruelle`() {
        // J1 à J5 (daysSince 0..4) -> menstruelle
        val result = CyclePhaseCalculator.calculate(setOf(start), start.plusDays(2), CycleProfile.REGULIER)
        assertEquals(CyclePhase.MENSTRUELLE, result)
    }

    @Test
    fun `apres les regles avant ovulation la phase est folliculaire`() {
        val result = CyclePhaseCalculator.calculate(setOf(start), start.plusDays(7), CycleProfile.REGULIER)
        assertEquals(CyclePhase.FOLLICULAIRE, result)
    }

    @Test
    fun `au milieu du cycle la phase est ovulation`() {
        // Cycle 28 par défaut -> ovulation ~ J14 (index 14)
        val result = CyclePhaseCalculator.calculate(setOf(start), start.plusDays(14), CycleProfile.REGULIER)
        assertEquals(CyclePhase.OVULATION, result)
    }

    @Test
    fun `en fin de cycle la phase est luteale`() {
        val result = CyclePhaseCalculator.calculate(setOf(start), start.plusDays(24), CycleProfile.REGULIER)
        assertEquals(CyclePhase.LUTEALE, result)
    }

    @Test
    fun `un retard anormal donne indeterminee`() {
        // 60 jours depuis les dernières règles > seuil de 45
        val result = CyclePhaseCalculator.calculate(setOf(start), start.plusDays(60), CycleProfile.IRREGULIER)
        assertEquals(CyclePhase.INDETERMINEE, result)
    }

    @Test
    fun `la longueur moyenne du cycle decale l'ovulation`() {
        // Deux débuts de règles espacés de 30 jours -> cycle moyen 30, ovulation ~ J16
        val periods = setOf(start, start.plusDays(30))
        // À J14 (index 14) du dernier début, on est encore folliculaire (ovulation décalée à 16)
        val result = CyclePhaseCalculator.calculate(periods, start.plusDays(30 + 14), CycleProfile.REGULIER)
        assertEquals(CyclePhase.FOLLICULAIRE, result)
    }

    @Test
    fun `les jours de regles consecutifs comptent comme un seul debut`() {
        // 3 jours consécutifs = un seul épisode ; J3 reste menstruel
        val periods = setOf(start, start.plusDays(1), start.plusDays(2))
        val result = CyclePhaseCalculator.calculate(periods, start.plusDays(2), CycleProfile.REGULIER)
        assertEquals(CyclePhase.MENSTRUELLE, result)
    }
}
