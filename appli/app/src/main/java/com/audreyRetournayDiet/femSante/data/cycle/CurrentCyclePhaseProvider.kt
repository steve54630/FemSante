package com.audreyRetournayDiet.femSante.data.cycle

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.CycleRepository
import com.audreyRetournayDiet.femSante.shared.UserStore
import java.time.LocalDate
import javax.inject.Inject

/**
 * Calcule la **phase du cycle courante** de l'utilisatrice à une date donnée, en réunissant les
 * données nécessaires (jours de règles + profil/durées déclarés) avant de déléguer le calcul à
 * [CyclePhaseCalculator].
 *
 * Extrait pour être **réutilisé** par les écrans qui personnalisent selon la phase (recette du
 * jour dans « Pour toi », filtre « Ma phase » de la navigation recettes) sans dupliquer le
 * câblage cycle.
 */
class CurrentCyclePhaseProvider @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val userStore: UserStore
) {

    /** Phase estimée pour [date] (aujourd'hui par défaut), ou `null` si non exploitable. */
    suspend fun currentPhase(date: LocalDate = LocalDate.now()): CyclePhase? {
        val userId = userStore.getUser()?.id ?: ""
        val periodDates = (cycleRepository.getPeriodDates(userId) as? ApiResult.Success)?.data ?: emptySet()
        return CyclePhaseCalculator.calculate(
            periodDates = periodDates,
            target = date,
            profile = userStore.getCycleProfile(),
            cycleLength = userStore.getCycleLength(),
            periodLength = userStore.getPeriodLength()
        )
    }
}
