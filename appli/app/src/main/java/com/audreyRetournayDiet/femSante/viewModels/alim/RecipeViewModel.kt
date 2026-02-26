package com.audreyRetournayDiet.femSante.viewModels.alim

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.RecipeUiState
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecetteViewModel(
    private val initialTitle: String,
    private val recipeMap: HashMap<String, String>,
    private val folderPath: String,
    private val getResourceId: (String) -> Int
) : ViewModel() {

    private val internalUiState = MutableStateFlow(RecipeUiState(title = initialTitle, recipeNames = recipeMap.values.toList()))
    val uiState: StateFlow<RecipeUiState> = internalUiState.asStateFlow()

    private val navigationSharedFlow = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = navigationSharedFlow.asSharedFlow()

    private var currentSearchKey: String? = null

    fun onRecipeSelected(displayName: String) {
        // Extraction de la clé brute
        val rawKey = recipeMap.filterValues { it == displayName }.keys.toString()

        // Nettoyage de la clé via ton utilitaire (Logique métier/technique)
        val cleanKey = Utilitaires.cleanKey(rawKey)

        currentSearchKey = cleanKey

        // Récupération de l'ID via la fonction passée par la Factory
        val resId = getResourceId(cleanKey)

        internalUiState.value = internalUiState.value.copy(
            isRecipeSelected = true,
            imageResourceId = (if (resId != 0) resId else 0)
        )
    }

    fun onOpenPdfClicked() {
        currentSearchKey?.let { key ->
            viewModelScope.launch {
                navigationSharedFlow.emit("$folderPath/$key.pdf")
            }
        }
    }

    class Factory(
        private val context: Context,
        private val title: String,
        private val map: HashMap<String, String>,
        private val path: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecetteViewModel(title, map, path) { search ->
                context.resources.getIdentifier(search, "drawable", context.packageName)
            } as T
        }
    }
}