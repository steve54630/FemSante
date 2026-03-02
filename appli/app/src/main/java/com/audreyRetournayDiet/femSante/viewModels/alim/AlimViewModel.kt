package com.audreyRetournayDiet.femSante.viewModels.alim

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.RecipeNavigationEvent
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.RecipeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AlimViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val tag = "VM_ALIM"

    private val navigationSharedFlow = MutableSharedFlow<RecipeNavigationEvent>()
    val navigationEvent: SharedFlow<RecipeNavigationEvent> = navigationSharedFlow.asSharedFlow()

    private val errorSharedFlow = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = errorSharedFlow.asSharedFlow()

    fun onCategorySelected(folderName: String, displayTitle: String, context: Context) {
        viewModelScope.launch {
            Log.d(tag, "Catégorie sélectionnée : $folderName ($displayTitle)")

            when (val result = repository.getRecipesFromAssets(folderName, context)) {
                is ApiResult.Success -> {
                    val rawFiles = result.data ?: emptyList()

                    val formattedMap = rawFiles.associate { fileName ->
                        val key = fileName.substringBeforeLast(".")
                        val value = key.replace("_", " ")
                            .replaceFirstChar { it.uppercase() }
                        key to value
                    }

                    Log.i(tag, "Mapping réussi : ${formattedMap.size} recettes prêtes pour l'affichage")

                    navigationSharedFlow.emit(
                        RecipeNavigationEvent(
                            title = displayTitle,
                            recipeMap = HashMap(formattedMap),
                            folderPath = folderName
                        )
                    )
                }
                is ApiResult.Failure -> {
                    Log.e(tag, "Échec du chargement des recettes pour le dossier : $folderName | Erreur : ${result.message}")
                    errorSharedFlow.emit(result.message)
                }
            }
        }
    }

    class AlimViewModelFactory(
        private val repository: RecipeRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlimViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AlimViewModel(repository) as T
            }
            Log.e("VM_FACTORY", "Impossible de créer AlimViewModel : Classe inconnue")
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}