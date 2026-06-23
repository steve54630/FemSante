package com.audreyRetournayDiet.femSante.viewModels.calendar

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.CycleRepository
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.type.FlowLevel
import com.audreyRetournayDiet.femSante.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dailyRepository = mockk<DailyRepository>(relaxed = true)
    private val cycleRepository = mockk<CycleRepository>(relaxed = true)

    private val vm by lazy { CalendarViewModel(dailyRepository, cycleRepository) }
    private val userId = "u1"
    private val date = LocalDate.of(2024, 6, 10)

    @Test
    fun `initData charge les dates de regles`() = runTest {
        coEvery { dailyRepository.getCalendarStatus(userId) } returns ApiResult.Success(emptyList(), "ok")
        coEvery { cycleRepository.getPeriodDates(userId) } returns ApiResult.Success(setOf(date), "ok")

        vm.initData(userId)

        assertEquals(setOf(date), vm.periodDates.value)
    }

    @Test
    fun `loadData met a jour la date et l'observation de cycle`() = runTest {
        val cycleDay = CycleDayEntity(userId = userId, date = 0L, isPeriod = true, flow = FlowLevel.MOYEN)
        coEvery { dailyRepository.getDailyEntryByDate(userId, date) } returns ApiResult.Success(null, "ok")
        coEvery { cycleRepository.getCycleDay(userId, date) } returns ApiResult.Success(cycleDay, "ok")

        vm.loadData(userId, date)

        assertEquals(date, vm.date.value)
        assertEquals(cycleDay, vm.cycleDay.value)
    }

    @Test
    fun `saveCycleDay ajoute la date aux jours de regles`() = runTest {
        val saved = CycleDayEntity(userId = userId, date = 0L, isPeriod = true)
        coEvery { cycleRepository.saveCycleDay(userId, date, true, null, false) } returns ApiResult.Success("ok", "msg")
        coEvery { cycleRepository.getCycleDay(userId, date) } returns ApiResult.Success(saved, "ok")

        vm.saveCycleDay(userId, date, isPeriod = true, flow = null, spotting = false)

        assertTrue(date in vm.periodDates.value)
    }
}
