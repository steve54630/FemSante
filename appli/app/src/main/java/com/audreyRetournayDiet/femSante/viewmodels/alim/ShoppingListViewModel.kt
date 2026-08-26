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
            val sections = ShoppingListBuilder.build(recipes.map { RecipeSelection(it.recipe, it.count) })
            // On ne garde que les ingrédients réellement présents aujourd'hui : un nom coché pour
            // une recette depuis retirée ne doit pas apparaître "déjà pris" si une autre recette
            // réutilise le même nom d'ingrédient plus tard.
            val currentNames = sections.flatMap { it.items }.map { it.name }.toSet()
            ShoppingListUiState(
                recipes = recipes,
                sections = sections,
                checked = checked intersect currentNames,
                isEmpty = recipes.isEmpty()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingListUiState())

    init {
        // Nettoie les sélections "fantômes" : une recette retirée du catalogue depuis son ajout
        // resterait sinon indéfiniment dans le store sans jamais s'afficher.
        viewModelScope.launch {
            store.selections.collect { selections ->
                selections.keys.filter { recipeRepository.getById(it) == null }.forEach { store.remove(it) }
            }
        }
    }

    fun increment(id: String, current: Int) = viewModelScope.launch { store.setCount(id, current + 1) }
    fun decrement(id: String, current: Int) = viewModelScope.launch { store.setCount(id, current - 1) }
    fun toggleChecked(name: String, checked: Boolean) = viewModelScope.launch { store.setChecked(name, checked) }
    fun clear() = viewModelScope.launch { store.clear() }
}
