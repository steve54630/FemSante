package com.audreyRetournayDiet.femSante.viewmodels.alim

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.recipe.Recipe
import com.audreyRetournayDiet.femSante.repository.local.RecipeContentRepository
import com.audreyRetournayDiet.femSante.repository.local.ShoppingListStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de la fiche recette native.
 *
 * L'identifiant de la recette ([EXTRA_RECIPE_ID]) provient de l'Intent via le
 * [SavedStateHandle]. Le contenu étant statique, la recette est résolue une fois ; `null`
 * signifie « recette absente du catalogue » (l'écran bascule alors sur le PDF de repli).
 *
 * Gère aussi l'appartenance de la recette à la liste de courses (bouton « Ajouter à ma liste »).
 */
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    repository: RecipeContentRepository,
    private val shoppingListStore: ShoppingListStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeId: String = savedStateHandle.get<String>(EXTRA_RECIPE_ID).orEmpty()

    val recipe: Recipe? = repository.getById(recipeId)

    /** `true` si la recette figure déjà dans la liste de courses. */
    val inShoppingList: StateFlow<Boolean> = shoppingListStore.selections
        .map { it.containsKey(recipeId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Ajoute la recette à la liste, ou l'en retire si elle y est déjà. Renvoie l'état résultant. */
    fun toggleInShoppingList(onResult: (added: Boolean) -> Unit) {
        viewModelScope.launch {
            val alreadyIn = shoppingListStore.selections.first().containsKey(recipeId)
            if (alreadyIn) shoppingListStore.remove(recipeId) else shoppingListStore.add(recipeId)
            onResult(!alreadyIn)
        }
    }

    companion object {
        const val EXTRA_RECIPE_ID = "RECIPE_ID"
    }
}
