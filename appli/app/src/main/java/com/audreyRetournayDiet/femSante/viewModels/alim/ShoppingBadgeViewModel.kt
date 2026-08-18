package com.audreyRetournayDiet.femSante.viewModels.alim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.local.ShoppingListStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Expose le nombre de recettes de la liste de courses, pour le **badge** de l'onglet « Ma liste ».
 *
 * Passe par un ViewModel (injection **par constructeur**) plutôt qu'une injection de champ dans
 * l'activité : l'injection de champ Hilt déclenche une lecture de métadonnées Kotlin incompatible
 * avec la version de Dagger utilisée.
 */
@HiltViewModel
class ShoppingBadgeViewModel @Inject constructor(
    store: ShoppingListStore
) : ViewModel() {

    val recipeCount: StateFlow<Int> = store.selections
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
