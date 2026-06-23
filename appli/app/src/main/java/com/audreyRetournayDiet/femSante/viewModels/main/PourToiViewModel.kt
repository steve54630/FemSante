package com.audreyRetournayDiet.femSante.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.recommendation.Recommendation
import com.audreyRetournayDiet.femSante.data.recommendation.RecommendationEngine
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

/**
 * État de la section "Pour toi aujourd'hui" affichée sur l'écran d'accueil.
 *
 * @param hasEntryToday Indique si une saisie existe pour la date du jour (et non une
 * entrée plus ancienne récupérée en repli) — pilote l'affichage du bandeau incitatif.
 */
data class PourToiUiState(
    val recommendations: List<Recommendation> = emptyList(),
    val hasEntryToday: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * ViewModel de la section additive "Pour toi aujourd'hui" sur l'écran d'accueil.
 *
 * Ne bloque jamais l'affichage : en l'absence de saisie du jour, [RecommendationEngine]
 * retourne l'intégralité du contenu sans filtrage (cf. garde-fou : le journal n'est pas
 * un péage).
 */
class PourToiViewModel(
    private val repository: DailyRepository,
    private val userId: String
) : ViewModel() {

    private val internalUiState = MutableStateFlow(PourToiUiState())
    val uiState: StateFlow<PourToiUiState> = internalUiState.asStateFlow()

    init {
        load()
    }

    /**
     * Recharge les recommandations. À appeler quand on revient sur le menu (onResume),
     * pour refléter une saisie de journal faite entre-temps dans le calendrier.
     */
    fun refresh() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val today = LocalDate.now()
            when (val result = repository.getLatestEntry(userId, today)) {
                is ApiResult.Success -> {
                    val latestEntry = result.data
                    val hasEntryToday = latestEntry?.dailyEntry?.date == dateToTimestamp(today)

                    internalUiState.value = PourToiUiState(
                        // Pas de badge "Recommandé" sur du contenu choisi au hasard :
                        // tant qu'aucune entrée n'existe, le reste du menu (inchangé)
                        // montre déjà tout le contenu sans filtrage.
                        recommendations = if (latestEntry != null) {
                            RecommendationEngine.recommend(latestEntry)
                        } else {
                            emptyList()
                        },
                        hasEntryToday = hasEntryToday,
                        isLoading = false
                    )
                }
                is ApiResult.Failure -> {
                    Timber.e("Échec chargement recommandations : ${result.message}")
                    // En cas d'erreur, on n'affiche pas de recommandation plutôt que
                    // d'en montrer une potentiellement fausse — le reste du menu reste
                    // accessible normalement.
                    internalUiState.value = PourToiUiState(
                        recommendations = emptyList(),
                        hasEntryToday = false,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun dateToTimestamp(date: LocalDate): Long {
        return date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    class Factory(
        private val repository: DailyRepository,
        private val userId: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PourToiViewModel(repository, userId) as T
        }
    }
}
