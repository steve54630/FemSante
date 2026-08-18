package com.audreyRetournayDiet.femSante.viewmodels.alim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.recipe.Recipe
import com.audreyRetournayDiet.femSante.data.recipe.RecipeSelection
import com.audreyRetournayDiet.femSante.data.recipe.ShoppingListBuilder
import com.audreyRetournayDiet.femSante.data.recipe.ShoppingSection
import com.audreyRetournayDiet.femSante.repository.local.RecipeContentRepository
import com.audreyRetournayDiet.femSante.repository.local.ShoppingListStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Une recette de la liste, avec son nombre de réalisations (pour le compteur `− N +`). */
data class ShoppingRecipe(val recipe: Recipe, val count: Int)

data class ShoppingListUiState(
    val recipes: List<ShoppingRecipe> = emptyList(),
    val sections: List<ShoppingSection> = emptyList(),
    val checked: Set<String> = emptySet(),
    val isEmpty: Boolean = true
)

/**
 * ViewModel de l'écran « Ma liste de courses » : combine les recettes retenues (avec leur
 * nombre) et les ingrédients cochés, puis reconstruit la liste groupée par rayon via
 * [ShoppingListBuilder]. Tout changement du store ré-émet l'état.
 */
@HiltViewModel
class ShoppingListViewModel @Inject constructor(
    private val store: ShoppingListStore,
    private val recipeRepository: RecipeContentRepository
) : ViewModel() {

    val uiState: StateFlow<ShoppingListUiState> =
        combine(store.selections, store.checkedNames) { selections, checked ->
            val recipes = selections.mapNotNull { (id, count) ->
                recipeRepository.getById(id)?.let { ShoppingRecipe(it, count) }
            }
            ShoppingListUiState(
                recipes = recipes,
                sections = ShoppingListBuilder.build(recipes.map { RecipeSelection(it.recipe, it.count) }),
                checked = checked,
                isEmpty = recipes.isEmpty()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingListUiState())

    fun increment(id: String, current: Int) = viewModelScope.launch { store.setCount(id, current + 1) }
    fun decrement(id: String, current: Int) = viewModelScope.launch { store.setCount(id, current - 1) }
    fun toggleChecked(name: String, checked: Boolean) = viewModelScope.launch { store.setChecked(name, checked) }
    fun clear() = viewModelScope.launch { store.clear() }
}
