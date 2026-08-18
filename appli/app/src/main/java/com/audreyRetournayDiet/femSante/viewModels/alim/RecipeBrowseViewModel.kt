package com.audreyRetournayDiet.femSante.viewModels.alim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.cycle.CurrentCyclePhaseProvider
import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase
import com.audreyRetournayDiet.femSante.data.recipe.Recipe
import com.audreyRetournayDiet.femSante.data.recipe.RecipeCategory
import com.audreyRetournayDiet.femSante.data.recipe.RecipeFilter
import com.audreyRetournayDiet.femSante.data.recipe.toRecipePhaseLabel
import com.audreyRetournayDiet.femSante.repository.local.RecipeContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * État de l'écran de navigation des recettes.
 *
 * @param phaseAvailable indique si le filtre « Ma phase » doit être proposé (phase du cycle
 *   exploitable). On ne l'affiche pas quand la phase est inconnue, pour ne pas offrir un filtre
 *   sans effet clair.
 */
data class RecipeBrowseUiState(
    val recipes: List<Recipe> = emptyList(),
    val category: RecipeCategory? = null,
    val phaseAvailable: Boolean = false,
    val phaseOnly: Boolean = false,
    val selectedTags: Set<String> = emptySet()
)

/**
 * ViewModel de l'écran recettes : garde l'état des filtres (catégorie, « Ma phase », tags) et
 * recalcule la liste affichée via [RecipeFilter]. Le catalogue et la phase du jour sont chargés
 * une fois ; chaque changement de filtre ré-émet l'état.
 */
@HiltViewModel
class RecipeBrowseViewModel @Inject constructor(
    recipeRepository: RecipeContentRepository,
    private val phaseProvider: CurrentCyclePhaseProvider
) : ViewModel() {

    private val allRecipes: List<Recipe> = recipeRepository.getAll()
    private var currentPhase: CyclePhase? = null

    private var category: RecipeCategory? = null
    private var phaseOnly: Boolean = false
    private var selectedTags: Set<String> = emptySet()

    private val internalState = MutableStateFlow(RecipeBrowseUiState(recipes = allRecipes))
    val uiState: StateFlow<RecipeBrowseUiState> = internalState.asStateFlow()

    init {
        viewModelScope.launch {
            currentPhase = phaseProvider.currentPhase()
            recompute()
        }
    }

    /** Sélectionne une catégorie (`null` = toutes). */
    fun setCategory(category: RecipeCategory?) {
        this.category = category
        recompute()
    }

    /** Active/désactive le filtre « recettes de ma phase ». */
    fun setPhaseOnly(enabled: Boolean) {
        phaseOnly = enabled
        recompute()
    }

    /** Remplace l'ensemble des tags diététiques sélectionnés (issus de la feuille de filtres). */
    fun setSelectedTags(tags: Set<String>) {
        selectedTags = tags
        recompute()
    }

    /**
     * Nombre de recettes qui resteraient affichées si [tags] était appliqué (catégorie et
     * « Ma phase » courants inchangés). Sert à l'aperçu live du bouton « Voir les résultats (n) »
     * de la feuille de filtres.
     */
    fun previewCount(tags: Set<String>): Int =
        RecipeFilter.filter(allRecipes, category, tags, currentPhase, phaseOnly).size

    private fun recompute() {
        internalState.value = RecipeBrowseUiState(
            recipes = RecipeFilter.filter(allRecipes, category, selectedTags, currentPhase, phaseOnly),
            category = category,
            phaseAvailable = currentPhase.toRecipePhaseLabel() != null,
            phaseOnly = phaseOnly,
            selectedTags = selectedTags
        )
    }
}
