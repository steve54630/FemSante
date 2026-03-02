package com.audreyRetournayDiet.femSante.viewModels.alim

import android.content.Context
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
import timber.log.Timber

/**
 * ViewModel gérant la logique métier de la section Alimentation / Recettes.
 * * ### Rôle :
 * 1. Communiquer avec le [RecipeRepository] pour lister les fichiers du dossier Assets.
 * 2. Formater les noms de fichiers techniques en titres affichables.
 * 3. Émettre des événements de navigation via [SharedFlow] pour l'UI.
 */
class AlimViewModel(private val repository: RecipeRepository) : ViewModel() {

    // Événements de navigation (utilisés pour passer à l'écran de liste des recettes)
    private val navigationSharedFlow = MutableSharedFlow<RecipeNavigationEvent>()
    val navigationEvent: SharedFlow<RecipeNavigationEvent> = navigationSharedFlow.asSharedFlow()

    // Événements d'erreur (utilisés pour afficher un Toast ou une Snackbar)
    private val errorSharedFlow = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = errorSharedFlow.asSharedFlow()

    /**
     * Traite la sélection d'une catégorie (ex: "Petit-déjeuner", "Déjeuner").
     * * @param folderName Le nom technique du dossier dans les assets.
     * @param displayTitle Le titre à afficher en haut de l'écran suivant.
     * @param context Contexte nécessaire pour accéder aux Assets.
     */
    fun onCategorySelected(folderName: String, displayTitle: String, context: Context) {
        viewModelScope.launch {
            Timber.d("Catégorie sélectionnée : $folderName ($displayTitle)")

            when (val result = repository.getRecipesFromAssets(folderName, context)) {
                is ApiResult.Success -> {
                    val rawFiles = result.data ?: emptyList()

                    // Transformation : "nom_du_fichier.json" -> "Nom du fichier"
                    val formattedMap = rawFiles.associate { fileName ->
                        val key = fileName.substringBeforeLast(".") // Enlève l'extension
                        val value = key.replace("_", " ") // Remplace underscores par espaces
                            .replaceFirstChar { it.uppercase() } // Majuscule sur la première lettre
                        key to value
                    }

                    Timber.i("Mapping réussi : ${formattedMap.size} recettes trouvées")

                    // On émet l'événement pour que l'Activity/Fragment change d'écran
                    navigationSharedFlow.emit(
                        RecipeNavigationEvent(
                            title = displayTitle,
                            recipeMap = HashMap(formattedMap),
                            folderPath = folderName
                        )
                    )
                }
                is ApiResult.Failure -> {
                    Timber.e("Erreur lors du chargement : ${result.message}")
                    errorSharedFlow.emit(result.message)
                }
            }
        }
    }

    /**
     * Factory pour instancier le ViewModel avec ses dépendances (Repository).
     */
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