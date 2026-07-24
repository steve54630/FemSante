package com.audreyRetournayDiet.femSante.viewModels.calendar

import com.audreyRetournayDiet.femSante.data.entities.AppUser
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.CycleRepository
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Tests du [CalendarViewModel] réactif : la grille et la journée sélectionnée sont exposées
 * via des `Flow` Room ; le prévisionnel est recalculé par [CalendarViewModel.refreshForecast].
 */
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DailyRepository>(relaxed = true)
    private val cycleRepository = mockk<CycleRepository>(relaxed = true)
    private val userStore = mockk<UserStore>(relaxed = true)

    private val userId = "u1"
    private val date = LocalDate.of(2024, 6, 10)

    private fun buildVm(): CalendarViewModel {
        every { userStore.getUser() } returns AppUser(userId, false, "e@e.e", "pwd")
        return CalendarViewModel(repository, cycleRepository, userStore)
    }

    @Test
    fun `selectDate met a jour la date observee`() = runTest {
        val vm = buildVm()

        vm.selectDate(date)

        assertEquals(date, vm.date.value)
    }

    @Test
    fun `cycleDay observe la journee selectionnee`() = runTest {
        val cycleDay = CycleDayEntity(userId = userId, date = 0L, isPeriod = true)
        every { cycleRepository.observeCycleDay(userId, date) } returns flowOf(cycleDay)

        val vm = buildVm()
        vm.selectDate(date)
        // WhileSubscribed : il faut un collecteur actif pour démarrer le flux amont. On le
        // lance sur Dispatchers.Main (UnconfinedTestDispatcher = eager), pas sur le scope de
        // runTest (StandardTestDispatcher = paresseux), sinon le collecteur ne démarre pas.
        val job = launch(Dispatchers.Main) { vm.cycleDay.collect {} }

        assertEquals(cycleDay, vm.cycleDay.value)
        job.cancel()
    }

    @Test
    fun `refreshForecast projette les regles pour un profil regulier`() = runTest {
        every { userStore.getCycleProfile() } returns CycleProfile.REGULIER
        every { userStore.getCycleLength() } returns 28
        every { userStore.getPeriodLength() } returns 5
        coEvery { cycleRepository.getPeriodDates(userId) } returns ApiResult.Success(setOf(date), "ok")
        coEvery { cycleRepository.getRecordedDates(userId) } returns ApiResult.Success(emptySet(), "ok")

        val vm = buildVm()
        vm.refreshForecast()

        // Fenêtre prédite : dernier début (date) + longueur de cycle déclarée (28 j).
        assertTrue(date.plusDays(28) in vm.predictedPeriodDates.value)
    }

    @Test
    fun `refreshForecast ne predit rien pour un profil irregulier`() = runTest {
        every { userStore.getCycleProfile() } returns CycleProfile.IRREGULIER
        every { userStore.getCycleLength() } returns 28
        every { userStore.getPeriodLength() } returns 5
        coEvery { cycleRepository.getPeriodDates(userId) } returns ApiResult.Success(setOf(date), "ok")
        coEvery { cycleRepository.getRecordedDates(userId) } returns ApiResult.Success(emptySet(), "ok")

        val vm = buildVm()
        vm.refreshForecast()

        assertTrue(vm.predictedPeriodDates.value.isEmpty())
    }

    @Test
    fun `saveCycleDay enregistre l'observation pour la date selectionnee`() = runTest {
        every { userStore.getCycleProfile() } returns CycleProfile.IRREGULIER
        every { userStore.getCycleLength() } returns 28
        every { userStore.getPeriodLength() } returns 5
        coEvery { cycleRepository.saveCycleDay(userId, date, true, null, false) } returns ApiResult.Success("ok", "")
        coEvery { cycleRepository.getPeriodDates(userId) } returns ApiResult.Success(emptySet(), "ok")
        coEvery { cycleRepository.getRecordedDates(userId) } returns ApiResult.Success(emptySet(), "ok")

        val vm = buildVm()
        vm.selectDate(date)
        vm.saveCycleDay(isPeriod = true, flow = null, spotting = false)

        coVerify { cycleRepository.saveCycleDay(userId, date, true, null, false) }
    }

    @Test
    fun `deleteData supprime l'entree via le repository`() = runTest {
        val entry = DailyEntryFull(
            dailyEntry = DailyEntryEntity(id = 5L, userId = userId, date = 0L),
            generalState = null,
            psychologicalState = null,
            symptomsState = null,
            contextState = null
        )
        coEvery { repository.deleteEntry(userId, 5L) } returns ApiResult.Success("ok", "")

        val vm = buildVm()
        vm.deleteData(entry)

        coVerify { repository.deleteEntry(userId, 5L) }
    }
}
