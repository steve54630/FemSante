package com.audreyRetournayDiet.femSante.viewModels.alim

import android.content.Context
import android.util.Log
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

class RecipeViewModel(
    initialTitle: String,
    private val recipeMap: HashMap<String, String>,
    private val folderPath: String,
    private val getResourceId: (String) -> Int
) : ViewModel() {

    private val tag = "VM_RECIPE"

    private val internalUiState = MutableStateFlow(RecipeUiState(title = initialTitle, recipeNames = recipeMap.values.toList()))
    val uiState: StateFlow<RecipeUiState> = internalUiState.asStateFlow()

    private val navigationSharedFlow = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = navigationSharedFlow.asSharedFlow()

    private var currentSearchKey: String? = null

    fun onRecipeSelected(displayName: String) {
        Log.d(tag, "Sélection de la recette : $displayName")

        val rawKey = recipeMap.filterValues { it == displayName }.keys.toString()
        val cleanKey = Utilitaires.cleanKey(rawKey)

        currentSearchKey = cleanKey

        val resId = getResourceId(cleanKey)

        if (resId == 0) {
            Log.w(tag, "Aucune image trouvée pour la clé : $cleanKey (drawable attendu)")
        } else {
            Log.d(tag, "Image chargée avec succès pour : $cleanKey (ResID: $resId)")
        }

        internalUiState.value = internalUiState.value.copy(
            isRecipeSelected = true,
            imageResourceId = resId
        )
    }

    fun onOpenPdfClicked() {
        currentSearchKey?.let { key ->
            val pdfPath = "$folderPath/$key.pdf"
            Log.i(tag, "Demande d'ouverture du PDF : $pdfPath")

            viewModelScope.launch {
                navigationSharedFlow.emit(pdfPath)
            }
        } ?: Log.e(tag, "Tentative d'ouverture PDF sans sélection de recette au préalable")
    }

    class Factory(
        private val context: Context,
        private val title: String,
        private val map: HashMap<String, String>,
        private val path: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Log.d("VM_FACTORY", "Initialisation RecipeViewModel pour la catégorie : $title")
            return RecipeViewModel(title, map, path) { search ->
                context.resources.getIdentifier(search, "drawable", context.packageName)
            } as T
        }
    }
}