package com.audreyRetournayDiet.femSante.viewModels.main

import com.audreyRetournayDiet.femSante.data.cycle.CurrentCyclePhaseProvider
import com.audreyRetournayDiet.femSante.data.entities.AppUser
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.repository.local.RecipeContentRepository
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import com.audreyRetournayDiet.femSante.room.type.PainZone
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class PourToiViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<DailyRepository>()
    private val recipeRepository = mockk<RecipeContentRepository>()
    private val phaseProvider = mockk<CurrentCyclePhaseProvider>()
    private val userStore = mockk<UserStore>()

    private fun stubUser() {
        every { userStore.getUser() } returns AppUser("u1", false, "a@b.c", "pwd")
    }

    /**
     * Neutralise la « recette du jour » (phase nulle, catalogue vide -> aucune recette) : ces
     * tests ne portent que sur les recommandations de contenu.
     */
    private fun stubRecipeOfDayDeps() {
        coEvery { phaseProvider.currentPhase(any()) } returns null
        every { recipeRepository.getAll() } returns emptyList()
    }

    private fun entryToday() = DailyEntryFull(
        dailyEntry = DailyEntryEntity(userId = "u1", date = 0L),
        generalState = GeneralStateEntity(entryId = 0L, painLevel = 2),
        psychologicalState = PsychologicalStateEntity(entryId = 0L),
        symptomsState = SymptomStateEntity(entryId = 0L, localizedPains = listOf(PainZone.BASSIN)),
        contextState = null
    )

    @Test
    fun `sans saisie aujourd'hui le bandeau s'affiche et aucune reco`() = runTest {
        stubUser()
        stubRecipeOfDayDeps()
        coEvery { repository.getDailyEntryByDate(any(), any<LocalDate>()) } returns ApiResult.Success(null, "ok")

        val vm = PourToiViewModel(repository, recipeRepository, phaseProvider, userStore)
        val state = vm.uiState.value

        assertFalse(state.hasEntryToday)
        assertTrue(state.recommendations.isEmpty())
    }

    @Test
    fun `avec saisie aujourd'hui des recommandations sont proposees`() = runTest {
        stubUser()
        stubRecipeOfDayDeps()
        coEvery { repository.getDailyEntryByDate(any(), any<LocalDate>()) } returns ApiResult.Success(entryToday(), "ok")

        val vm = PourToiViewModel(repository, recipeRepository, phaseProvider, userStore)
        val state = vm.uiState.value

        assertTrue(state.hasEntryToday)
        // L'entrée contient une douleur au bassin -> au moins un contenu tagué correspond.
        assertTrue(state.recommendations.isNotEmpty())
    }

    @Test
    fun `en cas d'echec depot aucune reco et pas de bandeau errone`() = runTest {
        stubUser()
        stubRecipeOfDayDeps()
        coEvery { repository.getDailyEntryByDate(any(), any<LocalDate>()) } returns ApiResult.Failure("erreur")

        val vm = PourToiViewModel(repository, recipeRepository, phaseProvider, userStore)
        val state = vm.uiState.value

        assertFalse(state.hasEntryToday)
        assertTrue(state.recommendations.isEmpty())
    }
}
