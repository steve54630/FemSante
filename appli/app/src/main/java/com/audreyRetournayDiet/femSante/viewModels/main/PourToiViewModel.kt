package com.audreyRetournayDiet.femSante.viewModels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.recommendation.Recommendation
import com.audreyRetournayDiet.femSante.data.recommendation.RecommendationEngine
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.shared.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

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
@HiltViewModel
class PourToiViewModel @Inject constructor(
    private val repository: DailyRepository,
    userStore: UserStore
) : ViewModel() {

    // L'identifiant utilisateur est lu une fois depuis le store injecté, plutôt que
    // passé manuellement via une Factory (ce qui rend ce ViewModel injectable par Hilt).
    private val userId: String = userStore.getUser()?.id ?: ""

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
            // Le carrousel "Pour toi aujourd'hui" reflète UNIQUEMENT la saisie du jour :
            // on charge l'entrée de la date locale du jour, pas la dernière entrée connue.
            // Ainsi, sans saisie aujourd'hui -> pas de recommandations + bandeau incitatif
            // (les deux sont mutuellement exclusifs).
            val today = LocalDate.now()
            when (val result = repository.getDailyEntryByDate(userId, today)) {
                is ApiResult.Success -> {
                    val todayEntry = result.data
                    internalUiState.value = PourToiUiState(
                        recommendations = if (todayEntry != null) {
                            RecommendationEngine.recommend(todayEntry)
                        } else {
                            emptyList()
                        },
                        hasEntryToday = todayEntry != null,
                        isLoading = false
                    )
                }
                is ApiResult.Failure -> {
                    Timber.e("Échec chargement recommandations : ${result.message}")
                    // En cas d'erreur, on n'affiche pas de recommandation plutôt que
                    // d'en montrer une potentiellement fausse.
                    internalUiState.value = PourToiUiState(
                        recommendations = emptyList(),
                        hasEntryToday = false,
                        isLoading = false
                    )
                }
            }
        }
    }
}
