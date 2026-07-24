package com.audreyRetournayDiet.femSante.data.cycle

import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Tests de la logique pure de prévisionnel des règles (incrément 3).
 * Aucune dépendance Android : exécution JVM rapide.
 */
class CyclePeriodForecasterTest {

    private val start = LocalDate.of(2024, 1, 1)

    @Test
    fun `profil irregulier ne predit rien`() {
        val result = CyclePeriodForecaster.predictPeriodDates(setOf(start), CycleProfile.IRREGULIER)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `profil absent ou pilule ne predit rien`() {
        val result = CyclePeriodForecaster.predictPeriodDates(setOf(start), CycleProfile.ABSENT_OU_PILULE)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `sans historique aucune prevision`() {
        val result = CyclePeriodForecaster.predictPeriodDates(emptySet(), CycleProfile.REGULIER)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `un seul debut projette les cycles suivants avec les valeurs par defaut`() {
        // Défaut : cycle 28, règles 5, horizon 3 -> 3 fenêtres de 5 jours.
        val result = CyclePeriodForecaster.predictPeriodDates(setOf(start), CycleProfile.REGULIER)

        assertEquals(15, result.size)
        assertTrue(start.plusDays(28) in result)      // 1er jour de la fenêtre 1
        assertTrue(start.plusDays(28 + 4) in result)  // 5e (dernier) jour de la fenêtre 1
        assertFalse(start.plusDays(28 + 5) in result) // hors fenêtre
        assertTrue(start.plusDays(56) in result)      // fenêtre 2
        assertTrue(start.plusDays(84) in result)      // fenêtre 3
    }

    @Test
    fun `la duree des regles declaree fixe la taille de la fenetre`() {
        val result = CyclePeriodForecaster.predictPeriodDates(
            periodDates = setOf(start),
            profile = CycleProfile.REGULIER,
            periodLength = 7,
            horizonCycles = 1
        )

        assertEquals(7, result.size)
        assertTrue(start.plusDays(28) in result)
        assertTrue(start.plusDays(28 + 6) in result)
        assertFalse(start.plusDays(28 + 7) in result)
    }

    @Test
    fun `la longueur de cycle declaree decale la fenetre sans historique`() {
        val result = CyclePeriodForecaster.predictPeriodDates(
            periodDates = setOf(start),
            profile = CycleProfile.REGULIER,
            cycleLength = 32,
            horizonCycles = 1
        )

        assertTrue(start.plusDays(32) in result)
        assertFalse(start.plusDays(28) in result)
    }

    @Test
    fun `avec assez d'historique la moyenne reelle prime sur la valeur declaree`() {
        // Deux débuts espacés de 30 jours -> cycle moyen 30, la valeur déclarée (28) est ignorée.
        val secondStart = start.plusDays(30)
        val result = CyclePeriodForecaster.predictPeriodDates(
            periodDates = setOf(start, secondStart),
            profile = CycleProfile.REGULIER,
            cycleLength = 28,
            horizonCycles = 1
        )

        assertTrue(secondStart.plusDays(30) in result)
        assertFalse(secondStart.plusDays(28) in result)
    }

    @Test
    fun `l'horizon limite le nombre de cycles projetes`() {
        val result = CyclePeriodForecaster.predictPeriodDates(
            periodDates = setOf(start),
            profile = CycleProfile.REGULIER,
            horizonCycles = 1
        )

        // Une seule fenêtre de 5 jours (défaut), pas de deuxième cycle.
        assertEquals(5, result.size)
        assertFalse(start.plusDays(56) in result)
    }

    @Test
    fun `les jours de regles consecutifs comptent comme un seul debut`() {
        // 3 jours consécutifs = un seul épisode ; la projection part de ce début unique.
        val periods = setOf(start, start.plusDays(1), start.plusDays(2))
        val result = CyclePeriodForecaster.predictPeriodDates(
            periodDates = periods,
            profile = CycleProfile.REGULIER,
            horizonCycles = 1
        )

        assertTrue(start.plusDays(28) in result)
    }
}
