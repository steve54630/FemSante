package com.audreyRetournayDiet.femSante.repository.local

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.room.dao.CycleDayDao
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.type.FlowLevel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Tests de [CycleRepository] avec un faux DAO en mémoire (pas de base réelle).
 */
class CycleRepositoryTest {

    /** Implémentation en mémoire du DAO, indexée par (userId, date) comme la vraie table. */
    private class FakeCycleDayDao : CycleDayDao {
        private val store = mutableListOf<CycleDayEntity>()

        override suspend fun upsert(day: CycleDayEntity): Long {
            store.removeAll { it.userId == day.userId && it.date == day.date }
            store.add(day)
            return 1L
        }

        override suspend fun getByDate(userId: String, date: Long): CycleDayEntity? =
            store.firstOrNull { it.userId == userId && it.date == date }

        override suspend fun getAll(userId: String): List<CycleDayEntity> =
            store.filter { it.userId == userId }.sortedBy { it.date }

        override suspend fun getPeriodDates(userId: String): List<Long> =
            store.filter { it.userId == userId && it.isPeriod }.map { it.date }
    }

    private val repository = CycleRepository(FakeCycleDayDao())
    private val userId = "u1"
    private val date = LocalDate.of(2024, 6, 10)

    @Test
    fun `enregistrer des regles puis relire conserve les donnees`() = runTest {
        repository.saveCycleDay(userId, date, isPeriod = true, flow = FlowLevel.MOYEN, spotting = false)

        val result = repository.getCycleDay(userId, date)
        assertTrue(result is ApiResult.Success)
        val day = (result as ApiResult.Success).data!!
        assertTrue(day.isPeriod)
        assertEquals(FlowLevel.MOYEN, day.flow)
        // La date doit correspondre au timestamp normalisé du jour
        assertEquals(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(), day.date)
    }

    @Test
    fun `le flux est ignore si pas de regles`() = runTest {
        // L'utilisatrice n'a pas ses règles mais un flux est passé : il doit être annulé.
        repository.saveCycleDay(userId, date, isPeriod = false, flow = FlowLevel.ABONDANT, spotting = true)

        val day = (repository.getCycleDay(userId, date) as ApiResult.Success).data!!
        assertNull(day.flow)
        assertTrue(day.spotting)
    }

    @Test
    fun `getPeriodDates ne renvoie que les jours de regles`() = runTest {
        repository.saveCycleDay(userId, date, isPeriod = true, flow = null, spotting = false)
        repository.saveCycleDay(userId, date.plusDays(10), isPeriod = false, flow = null, spotting = true)

        val dates = (repository.getPeriodDates(userId) as ApiResult.Success).data!!
        assertEquals(setOf(date), dates)
    }

    @Test
    fun `upsert ecrase l'observation du meme jour`() = runTest {
        repository.saveCycleDay(userId, date, isPeriod = true, flow = FlowLevel.LEGER, spotting = false)
        repository.saveCycleDay(userId, date, isPeriod = true, flow = FlowLevel.ABONDANT, spotting = false)

        val day = (repository.getCycleDay(userId, date) as ApiResult.Success).data!!
        assertEquals(FlowLevel.ABONDANT, day.flow)
    }
}
