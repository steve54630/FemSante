package com.audreyRetournayDiet.femSante.viewModels.alim

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.audreyRetournayDiet.femSante.data.recipe.Recipe
import com.audreyRetournayDiet.femSante.repository.local.RecipeContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel de la fiche recette native.
 *
 * L'identifiant de la recette ([EXTRA_RECIPE_ID]) provient de l'Intent via le
 * [SavedStateHandle]. Le contenu étant statique (chargé depuis les assets), la recette est
 * résolue une fois pour toutes ; `null` signifie « recette absente du catalogue » (l'écran
 * bascule alors sur le PDF de repli).
 */
@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    repository: RecipeContentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val recipe: Recipe? =
        repository.getById(savedStateHandle.get<String>(EXTRA_RECIPE_ID).orEmpty())

    companion object {
        const val EXTRA_RECIPE_ID = "RECIPE_ID"
    }
}
