package com.audreyRetournayDiet.femSante.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.cycle.CurrentCyclePhaseProvider
import com.audreyRetournayDiet.femSante.data.recipe.DailyRecipeSelector
import com.audreyRetournayDiet.femSante.data.recipe.RecipeOfDay
import com.audreyRetournayDiet.femSante.data.recommendation.ContentTagRepository
import com.audreyRetournayDiet.femSante.data.recommendation.Recommendation
import com.audreyRetournayDiet.femSante.data.recommendation.RecommendationEngine
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.repository.local.RecipeContentRepository
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
    val recipeOfDay: RecipeOfDay? = null,
    val hasEntryToday: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * ViewModel de la section "Pour toi aujourd'hui".
 *
 * Lecture **explicite** de la saisie du jour à chaque [refresh] (appelé en `onResume` du
 * menu) : requête `suspend` directe, sans dépendre de l'invalidation d'un `Flow` Room
 * (peu fiable avec SQLCipher). `today` est réévalué à chaque appel.
 *
 * La **recette du jour** dépend de la phase du cycle (et non de la saisie du journal) : elle est
 * donc proposée même en l'absence d'entrée du jour.
 */
@HiltViewModel
class PourToiViewModel @Inject constructor(
    private val repository: DailyRepository,
    private val recipeRepository: RecipeContentRepository,
    private val contentTagRepository: ContentTagRepository,
    private val phaseProvider: CurrentCyclePhaseProvider,
    private val userStore: UserStore // L'injecter en private val pour y accéder dans refresh()
) : ViewModel() {

    private val internalState = MutableStateFlow(PourToiUiState())
    val uiState: StateFlow<PourToiUiState> = internalState.asStateFlow()

    init {
        refresh()
    }

    /** Recharge la saisie du jour et recalcule les recommandations + la recette du jour. */
    fun refresh() {
        viewModelScope.launch {
            // 1. On indique le début du chargement (permet aussi de forcer la ré-émission du StateFlow)
            internalState.value = internalState.value.copy(isLoading = true)

            // 2. LECTURE À CHAUD du userId et de la date
            val currentUserId = userStore.getUser()?.id ?: ""
            val today = LocalDate.now()

            // 3. Recommandations de contenu, basées sur la saisie du jour (si elle existe)
            val entry = (repository.getDailyEntryByDate(currentUserId, today) as? ApiResult.Success)?.data
            val recos = entry?.let {
                RecommendationEngine.recommend(it, contentTagRepository.tagsByContent())
            } ?: emptyList()

            // 4. Recette du jour, basée sur la phase du cycle (indépendante de la saisie du jour)
            val phase = phaseProvider.currentPhase(today)
            val recipeOfDay = DailyRecipeSelector.select(recipeRepository.getAll(), phase, today)

            // 5. Émission du nouvel état
            internalState.value = PourToiUiState(
                recommendations = recos,
                recipeOfDay = recipeOfDay,
                hasEntryToday = entry != null,
                isLoading = false
            )
        }
    }
}
