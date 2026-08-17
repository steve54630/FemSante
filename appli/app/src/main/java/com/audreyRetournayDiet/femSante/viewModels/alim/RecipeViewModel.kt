package com.audreyRetournayDiet.femSante.viewModels.alim

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.RecipeOpenRequest
import com.audreyRetournayDiet.femSante.data.entities.RecipeUiState
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel gérant l'affichage d'une liste de recettes et le détail d'une sélection.
 *
 * Les paramètres (titre, map des recettes, dossier) proviennent des extras de l'Intent
 * via le [SavedStateHandle]. Le [Context] applicatif est injecté pour résoudre les images.
 */
@HiltViewModel
class RecipeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recipeMap: HashMap<String, String> =
        savedStateHandle.get<HashMap<String, String>>("map") ?: hashMapOf()
    private val folderPath: String = savedStateHandle.get<String>("FOLDER_PATH") ?: ""
    private val initialTitle: String = savedStateHandle.get<String>("Title") ?: "Recettes"

    // État de l'UI : contient la liste des noms, le titre, et l'image de la recette sélectionnée.
    private val internalUiState = MutableStateFlow(
        RecipeUiState(title = initialTitle, recipeNames = recipeMap.values.toList())
    )
    val uiState: StateFlow<RecipeUiState> = internalUiState.asStateFlow()

    // Événement de navigation : émet la recette à ouvrir (fiche native + PDF de repli).
    private val navigationSharedFlow = MutableSharedFlow<RecipeOpenRequest>()
    val navigationEvent: SharedFlow<RecipeOpenRequest> = navigationSharedFlow.asSharedFlow()

    // Stocke la clé technique de la recette actuellement sélectionnée.
    private var currentSearchKey: String? = null

    /**
     * Appelé quand l'utilisatrice clique sur une recette dans la liste.
     * Recherche l'image correspondante et met à jour l'état de l'écran.
     */
    fun onRecipeSelected(displayName: String) {
        Timber.d("Sélection de la recette : $displayName")

        // Retrouve la clé technique à partir du nom affiché
        val rawKey = recipeMap.filterValues { it == displayName }.keys.toString()
        val cleanKey = Utilitaires.cleanKey(rawKey)

        currentSearchKey = cleanKey

        // Tentative de récupération de l'image (drawable) portant le même nom que la clé
        val resId = getResourceId(cleanKey)
        // (getResourceId est maintenant une fonction membre utilisant le Context injecté)

        if (resId == 0) {
            Timber.w("Aucune image trouvée pour la clé : $cleanKey")
        } else {
            Timber.d("Image chargée avec succès (ResID: $resId)")
        }

        // Mise à jour de l'état pour afficher l'image et le bouton PDF
        internalUiState.value = internalUiState.value.copy(
            isRecipeSelected = true,
            imageResourceId = resId
        )
    }

    /**
     * Déclenche l'ouverture de la recette sélectionnée : fiche native si elle existe dans le
     * catalogue, avec le chemin du PDF d'origine en repli / impression.
     */
    fun onOpenRecipeClicked() {
        currentSearchKey?.let { key ->
            val pdfPath = "$folderPath/$key.pdf"
            Timber.i("Demande d'ouverture de la recette : $key (PDF de repli : $pdfPath)")

            viewModelScope.launch {
                navigationSharedFlow.emit(RecipeOpenRequest(recipeId = key, pdfPath = pdfPath))
            }
        } ?: Timber.e("Erreur : Aucune recette n'est sélectionnée.")
    }

    /**
     * Recherche dynamique de l'ID d'un drawable par son nom (remplace l'ancien lambda
     * injecté via Factory).
     */
    private fun getResourceId(name: String): Int =
        context.resources.getIdentifier(name, "drawable", context.packageName)
}