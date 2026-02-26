package com.audreyRetournayDiet.femSante.viewModels.alim

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.RecipeRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AlimViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val navigationSharedFlow = MutableSharedFlow<RecipeNavigationEvent>()
    val navigationEvent: SharedFlow<RecipeNavigationEvent> = navigationSharedFlow.asSharedFlow()

    private val errorSharedFlow = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = errorSharedFlow.asSharedFlow()

    fun onCategorySelected(folderName: String, displayTitle: String, context: Context) {
        viewModelScope.launch {
            // Le Repository doit déjà avoir le context via son constructeur, pas ici
            when (val result = repository.getRecipesFromAssets(folderName, context)) {
                is ApiResult.Success -> {
                    val formattedMap = result.data?.associate { fileName ->
                        val key = fileName.substringBeforeLast(".")
                        val value = key.replace("_", " ")
                            .replaceFirstChar { it.uppercase() }
                        key to value
                    } ?: emptyMap()

                    navigationSharedFlow.emit(
                        RecipeNavigationEvent(
                            title = displayTitle,
                            recipeMap = HashMap(formattedMap),
                            folderPath = folderName
                        )
                    )
                }
                is ApiResult.Failure -> {
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
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class RecipeNavigationEvent(
    val title: String,
    val recipeMap: HashMap<String, String>,
    val folderPath: String
)