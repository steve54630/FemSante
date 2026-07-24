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
import java.time.LocalDate
import javax.inject.Inject

/**
 * État de la section "Pour toi aujourd'hui" affichée sur l'écran d'accueil.
 */
data class PourToiUiState(
    val recommendations: List<Recommendation> = emptyList(),
    val hasEntryToday: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * ViewModel de la section "Pour toi aujourd'hui".
 *
 * Lecture **explicite** de la saisie du jour à chaque [refresh] (appelé en `onResume` du
 * menu) : requête `suspend` directe, sans dépendre de l'invalidation d'un `Flow` Room
 * (peu fiable avec SQLCipher). `today` est réévalué à chaque appel.
 */
@HiltViewModel
class PourToiViewModel @Inject constructor(
    private val repository: DailyRepository,
    private val userStore: UserStore // L'injecter en private val pour y accéder dans refresh()
) : ViewModel() {

    private val internalState = MutableStateFlow(PourToiUiState())
    val uiState: StateFlow<PourToiUiState> = internalState.asStateFlow()

    init {
        refresh()
    }

    /** Recharge la saisie du jour et recalcule les recommandations. */
    fun refresh() {
        viewModelScope.launch {
            // 1. On indique le début du chargement (permet aussi de forcer la ré-émission du StateFlow)
            internalState.value = internalState.value.copy(isLoading = true)

            // 2. LECTURE À CHAUD du userId et de la date
            val currentUserId = userStore.getUser()?.id ?: ""
            val today = LocalDate.now()

            // 3. Relecture dans la base de données
            val entry = (repository.getDailyEntryByDate(currentUserId, today) as? ApiResult.Success)?.data
            val recos = entry?.let { RecommendationEngine.recommend(it) } ?: emptyList()

            // 4. Émission du nouvel état
            internalState.value = PourToiUiState(
                recommendations = recos,
                hasEntryToday = entry != null,
                isLoading = false
            )
        }
    }
}
